#!/bin/sh
docker run -d --name=es -v $(PWD)/elastic/config:/usr/share/elasticsearch/config -v $(PWD)/data:/usr/share/elasticsearch/data -p 9200:9200 -p 9300:9300 elasticsearch -Etransport.host=0.0.0.0 -Ediscovery.zen.minimum_master_nodes=1
#docker run -d --name=es -p 9200:9200 -p 9300:9300 elasticsearch -Etransport.host=0.0.0.0 -Ediscovery.zen.minimum_master_nodes=1
