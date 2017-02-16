#!/bin/sh
#docker run -d --name=es -v $(PWD)/elastic/config:/usr/share/elasticsearch/config -v $(PWD)/elastic/data:/usr/share/elasticsearch/data -p 9200:9200 -p 9300:9300 elasticsearch -Etransport.host=0.0.0.0 -Ediscovery.zen.minimum_master_nodes=1
docker run -d --name=es -v $(pwd)/elastic/data:/usr/share/elasticsearch/data -v $(pwd)/elastic/logs:/usr/share/elasticsearch/logs -p 9200:9200 -p 9300:9300 elasticsearch
#docker run -d --name=kibana  --link es:localhost -e ELASTICSEARCH_URL=http://0.0.0.0:9200 -p 5601:5601 kibana
docker run -d --name=kibana  --link es:elasticsearch -p 5601:5601 kibana

# x-pack installation
docker exec es /usr/share/elasticsearch/bin/elasticsearch-plugin install x-pack
docker exec kibana /usr/share/kibana/bin/kibana-plugin install x-pack