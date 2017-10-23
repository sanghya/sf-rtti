%%%-------------------------------------------------------------------
%% @doc emq_custom_plugin public API
%% @end
%%%-------------------------------------------------------------------

-module(emq_custom_plugin_app).

-behaviour(application).

%% Application callbacks
-export([start/2, stop/1]).

%%====================================================================
%% API
%%====================================================================

start(_StartType, _StartArgs) ->
    {ok, Sup} = emq_custom_plugin_sup:start_link(),
	emq_custom_plugin:load(application:get_all_env()),
	{ok, Sup}.

%%--------------------------------------------------------------------
stop(_State) ->
	emq_custom_plugin:unload(),
    ok.

%%====================================================================
%% Internal functions
%%====================================================================
