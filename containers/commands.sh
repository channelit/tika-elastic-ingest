#!/bin/sh
#docker run -d --name=es -v $(PWD)/elastic/config:/usr/share/elasticsearch/config -v $(PWD)/elastic/data:/usr/share/elasticsearch/data -p 9200:9200 -p 9300:9300 elasticsearch -Etransport.host=0.0.0.0 -Ediscovery.zen.minimum_master_nodes=1
docker run -d --name=es -v $(PWD)/elastic/config:/usr/share/elasticsearch/config -v $(PWD)/elastic/data:/usr/share/elasticsearch/data -v $(pwd)/elastic/logs:/usr/share/elasticsearch/logs -p 9200:9200 -p 9300:9300 elasticsearch
docker run -d --name=kibana --link localhost:elasticsearch -p 5601:5601 kibana