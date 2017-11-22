%% @author PSJ
%% @doc @todo Add description to mod_hook_handler.

-module(smart_fleet_data_plugin).

-include_lib("emqttd/include/emqttd.hrl").

-import(smart_fleet_data_plugin_util, [sendMsgToKafka/3, makeDefaultMsg/3, makeDefaultMsgAndTopic/4]).
-import(smart_fleet_data_plugin_util, [makeFullMsg/3, partitionNum/2, getMsgField/2]).

-export([load/1, unload/0]).

-export([on_client_connected/3, on_client_disconnected/3]).
-export([on_client_subscribe/4, on_client_unsubscribe/4]).
-export([on_session_created/3, on_session_subscribed/4, on_session_unsubscribed/4, on_session_terminated/4]).
-export([on_message_publish/2, on_message_delivered/4, on_message_acked/4]).

load(Env) ->
  emqttd:hook('client.connected', fun ?MODULE:on_client_connected/3, [Env]),
  emqttd:hook('client.disconnected', fun ?MODULE:on_client_disconnected/3, [Env]),
  emqttd:hook('client.subscribe', fun ?MODULE:on_client_subscribe/4, [Env]),
  emqttd:hook('client.unsubscribe', fun ?MODULE:on_client_unsubscribe/4, [Env]),
  emqttd:hook('session.created', fun ?MODULE:on_session_created/3, [Env]),
  emqttd:hook('session.subscribed', fun ?MODULE:on_session_subscribed/4, [Env]),
  emqttd:hook('session.unsubscribed', fun ?MODULE:on_session_unsubscribed/4, [Env]),
  emqttd:hook('session.terminated', fun ?MODULE:on_session_terminated/4, [Env]),
  emqttd:hook('message.publish', fun ?MODULE:on_message_publish/2, [Env]),
  emqttd:hook('message.delivered', fun ?MODULE:on_message_delivered/4, [Env]),
  emqttd:hook('message.acked', fun ?MODULE:on_message_acked/4, [Env]).

on_client_connected(ConnAck, Client = #mqtt_client{client_id = ClientId}, Env) ->
    lager:debug("on_client_connected : ClientId ~s connected, connack: ~w ~w~n", [ClientId, ConnAck, Client]),
	ok = sendMsgToKafka(makeDefaultMsg("client.connected", Env, ClientId), partitionNum(ClientId, Env), Env),
    {ok, Client}.

on_client_disconnected(Reason, _Client = #mqtt_client{client_id = ClientId}, Env) ->
    lager:debug("on_client_disconnected : client ~s disconnected, reason: ~w~n", [ClientId, Reason]),
	ok = sendMsgToKafka(makeDefaultMsg("client.disconnected", Env, ClientId), partitionNum(ClientId, Env), Env),
    ok.

on_client_subscribe(ClientId, Username, TopicTable, _Env) ->
    lager:debug("on_client_subscribe :: client(~s/~s) will subscribe: ~p~n", [Username, ClientId, TopicTable]),
	%ok = brod:produce_sync(brod_client_1, <<"erlang-test">>, 0, <<"key1">>, <<"on_client_subscribe">>),
    {ok, TopicTable}.
    
on_client_unsubscribe(ClientId, Username, TopicTable, _Env) ->
    lager:debug("on_client_unsubscribe : client(~s/~s) unsubscribe ~p~n", [ClientId, Username, TopicTable]),
	%ok = brod:produce_sync(brod_client_1, <<"erlang-test">>, 0, <<"key1">>, <<"on_client_unsubscribe">>),
    {ok, TopicTable}.

on_session_created(ClientId, Username, _Env) ->
    lager:debug("on_session_created : session(~s/~s) created.", [ClientId, Username]).
	%ok = brod:produce_sync(brod_client_1, <<"erlang-test">>, 0, <<"key1">>, <<"on_session_created">>).

on_session_subscribed(ClientId, Username, {Topic, Opts}, Env) ->
    lager:debug("on_session_subscribed : session(~s/~s) subscribed: ~p~n", [Username, ClientId, {Topic, Opts}]),
	ok = sendMsgToKafka(makeDefaultMsgAndTopic("session.subscribed", Env, ClientId, Topic), partitionNum(ClientId, Env), Env),
    {ok, {Topic, Opts}}.

on_session_unsubscribed(ClientId, Username, {Topic, Opts}, Env) ->
    lager:debug("on_session_unsubscribed : session(~s/~s) unsubscribed: ~p~n", [Username, ClientId, {Topic, Opts}]),
	ok = sendMsgToKafka(makeDefaultMsgAndTopic("session.unsubscribed", Env, ClientId, Topic), partitionNum(ClientId, Env), Env),
    ok.

on_session_terminated(ClientId, Username, Reason, _Env) ->
    lager:debug("on_session_terminated : session(~s/~s) terminated: ~p.", [ClientId, Username, Reason]).
	%ok = brod:produce_sync(brod_client_1, <<"erlang-test">>, 0, <<"key1">>, <<"on_session_terminated">>).

%% transform message and return
on_message_publish(Message = #mqtt_message{topic = <<"$SYS/", _/binary>>}, _Env) ->
    {ok, Message};

on_message_publish(Message, Env) ->
    lager:debug("on_message_publish : publish ~s~n", [emqttd_message:format(Message)]),
	ok = sendMsgToKafka(makeFullMsg("message.publish", Env, Message), partitionNum(getMsgField("ClientId", Message), Env), Env),
    {ok, Message}.

on_message_delivered(ClientId, Username, Message, _Env) ->
    lager:debug("on_message_delivered : delivered to client(~s/~s): ~s~n", [Username, ClientId, emqttd_message:format(Message)]),
	%ok = brod:produce_sync(brod_client_1, <<"erlang-test">>, 0, <<"key1">>, <<"on_message_delivered">>),
    {ok, Message}.

on_message_acked(ClientId, Username, Message, _Env) ->
    lager:debug("on_message_acked : client(~s/~s) acked: ~s~n", [Username, ClientId, emqttd_message:format(Message)]),
	%ok = brod:produce_sync(brod_client_1, <<"erlang-test">>, 0, <<"key1">>, <<"on_message_acked">>),
    {ok, Message}.

unload() ->
  emqttd:unhook('client.connected', fun ?MODULE:on_client_connected/3),
  emqttd:unhook('client.disconnected', fun ?MODULE:on_client_disconnected/3),
  emqttd:unhook('client.subscribe', fun ?MODULE:on_client_subscribe/4),
  emqttd:unhook('client.unsubscribe', fun ?MODULE:on_client_unsubscribe/4),
  emqttd:unhook('session.subscribed', fun ?MODULE:on_session_subscribed/4),
  emqttd:unhook('session.unsubscribed', fun ?MODULE:on_session_unsubscribed/4),
  emqttd:unhook('message.publish', fun ?MODULE:on_message_publish/2),
  emqttd:unhook('message.delivered', fun ?MODULE:on_message_delivered/4),
  emqttd:unhook('message.acked', fun ?MODULE:on_message_acked/4).
