PROJECT = emq-custom-plugin
PROJECT_DESCRIPTION = EMQ Custom Plugin
PROJECT_VERSION = 1.0

BUILD_DEPS = emqttd
dep_emqttd = git https://github.com/emqtt/emqttd master

ERLC_OPTS += +debug_info
ERLC_OPTS += +'{parse_transform, lager_transform}'

COVER = true

include erlang.mk

app:: rebar.config
