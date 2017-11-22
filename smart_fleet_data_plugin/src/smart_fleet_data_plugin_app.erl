%%%-------------------------------------------------------------------
%% @doc smart_fleet_data_plugin public API
%% @end
%%%-------------------------------------------------------------------

-module(smart_fleet_data_plugin_app).

-include_lib("brod/include/brod.hrl").

-behaviour(application).

%% Application callbacks
-export([start/2, stop/1]).

%%====================================================================
%% API
%%====================================================================

start(_StartType, _StartArgs) ->
    {ok, Sup} = smart_fleet_data_plugin_sup:start_link(),
	Env = application:get_all_env(),
	smart_fleet_data_plugin:load(Env),
	lager:start(),
	ClientConfig = [{reconnect_cool_down_seconds, 10}],
	ok = brod:start_client(proplists:get_value(kafkaConAddr, Env, []), brod_client_1, ClientConfig),
	brod:start_producer(_Client = brod_client_1, _Topic = <<"erlang-test">>, _ProducerConfig = []),
	{ok, Sup}.

%%--------------------------------------------------------------------
stop(_State) ->
	smart_fleet_data_plugin:unload(),
    ok.

%%====================================================================
%% Internal functions
%%====================================================================
