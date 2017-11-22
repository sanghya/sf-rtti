PROJECT = smart_fleet_data_plugin
PROJECT_DESCRIPTION = v2x Custom Plugin
PROJECT_VERSION = 1.0

BUILD_DEPS = emqttd supervisor3 brod kafka_protocol
dep_emqttd = git https://github.com/emqtt/emqttd master
dep_brod = git https://github.com/klarna/brod master
dep_kafka_protocol = git https://github.com/klarna/kafka_protocol master
dep_supervisor3 = git https://github.com/klarna/supervisor3 master

ERLC_OPTS += +'{parse_transform, lager_transform}'

COVER = true

include erlang.mk

app:: rebar.config