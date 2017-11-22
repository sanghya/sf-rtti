%% @author PSJ
%% @doc @todo Add description to smart_fleet_data_plugin_util.


-module(smart_fleet_data_plugin_util).

-include_lib("emqttd/include/emqttd.hrl").
-include_lib("brod/include/brod.hrl").

-define(PARTITION_NUM, proplists:get_value(kafkaPartitionNum, application:get_all_env(?MODULE), 1)).
-define(HASH_TYPE, proplists:get_value(hashType, application:get_all_env(?MODULE), sha256)).
-define(BROKER_IP, proplists:get_value(brokerIp, application:get_all_env(?MODULE), "127.0.0.2")).

%% ====================================================================
%% API functions
%% ====================================================================
-export([sendMsgToKafka/3, makeDefaultMsg/3, makeDefaultMsgAndTopic/4, makeFullMsg/3]).
-export([partitionNum/2, getMsgField/2]).

%% ====================================================================
%% Internal functions
%% ====================================================================
sendMsgToKafka(Msg, Partition, Env) ->
	lager:debug("Partition:~w PARTITION_NUM:~w HASH_TYPE:~w BROKER_IP:~s~n", [Partition, proplists:get_value(kafkaPartitionNum, Env, 1), proplists:get_value(hashType, Env, sha256), proplists:get_value(brokerIp, Env, "127.0.0.1")]),
	lager:debug("Partition:~w PARTITION_NUM:~w HASH_TYPE:~w BROKER_IP:~s~n", [Partition, ?PARTITION_NUM, ?HASH_TYPE, ?BROKER_IP]),
	ok = brod:produce_sync(brod_client_1, proplists:get_value(kafkaTopic, Env, <<"">>), Partition, <<"key1">>, Msg),
	%msgWriteFile(proplists:get_value(rmFilePath, Env, "rmFile"), Msg),
	ok.

makeDefaultMsg(EventType, Env, ClientId) ->
	list_to_binary(["{\"broker\":\"" ++ proplists:get_value(brokerIp, Env, "127.0.0.1") ++ "\",\"hook\":\"" ++ EventType ++ "\",", timestamp(), ",\"clientID\":\"", binary_to_list(ClientId), "\"}"]).

makeDefaultMsgAndTopic(EventType, Env, ClientId, Topic) ->
	list_to_binary(["{\"broker\":\"" ++ proplists:get_value(brokerIp, Env, "127.0.0.1") ++ "\",\"hook\":\"" ++ EventType ++ "\",", timestamp(), ",\"clientID\":\"", binary_to_list(ClientId), "\",\"topic\":\"", binary_to_list(Topic), "\"}"]).

makeFullMsg(EventType, Env, #mqtt_message{id = _MsgId, pktid = _PktId, from = {ClientId, _Username}, qos = _Qos, retain = _Retain, dup = _Dup, topic =Topic, payload = Payload}) ->
	list_to_binary(["{\"broker\":\"" ++ proplists:get_value(brokerIp, Env, "127.0.0.1") ++ "\",\"hook\":\"" ++ EventType ++ "\",", timestamp(), ",\"clientID\":\"", ClientId, "\",\"topic\":\"", Topic,
					 "\",\"payload\":\"", Payload, "\"}"]).

getMsgField(FieldNm, #mqtt_message{id = _MsgId, pktid = _PktId, from = {ClientId, _Username}, qos = _Qos, retain = _Retain, dup = _Dup, topic =Topic, payload = Payload}) ->
	case FieldNm of
		"ClientId" -> ClientId
	end.

partitionNum(ClientId, Env) ->
	crypto:bytes_to_integer(crypto:hash(proplists:get_value(hashType, Env, sha256), binary_to_list(ClientId))) rem proplists:get_value(kafkaPartitionNum, Env, 1).

msgWriteFile(FilePath, Msg) ->
    case file:open(FilePath ++ "-" ++ dateFormat(), [append]) of
        {ok, IoDevice} ->
            io:format(IoDevice, "~s~n", [Msg]),
            file:close(IoDevice);
        {error, Reason} ->
            lager:error("~s open error  reason:~s~n", [FilePath, Reason])
    end.

timestamp() -> "\"timestamp\":" ++ integer_to_list(erlang:system_time(millisecond) div 1000).

dateFormat() ->
	{Year, Month, Day} = date(),
	lists:flatten(io_lib:format('~4..0b-~2..0b-~2..0b', [Year, Month, Day])).
