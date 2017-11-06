docker save dineshpillai/mu_zookeeper > mu_zookeeper.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/mu_zookeeper.tar docker@192.168.64.4:~/
#docker load < mu_zookeeper.tar

docker save innovation_hazelcast > mu_hazelcast.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/mu_hazelcast.tar docker@192.168.64.4:~/
#docker load < mu_hazelcast.tar

docker save dineshpillai/innovation-trade-imdg > trade-imdg.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/trade-imdg.tar docker@192.168.64.4:~/
#docker load < trade-imdg.tar

docker save dineshpillai/innovation-trade-injector > trade-injector.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/trade-injector.tar docker@192.168.64.4:~/
#docker load < trade-injector.tar

docker save dineshpillai/mu-pricequery-service > mu-pricequery-service.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/mu-pricequery-service.tar docker@192.168.64.4:~/
#docker load < mu-pricequery-service.tar

docker save dineshpillai/imcs-positionqueryservice > imcs-positionqueryservice.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/imcs-positionqueryservice.tar docker@192.168.64.4:~/
#docker load < imcs-positionqueryservice.tar

docker save dineshpillai/imcs-tradequeryservice > imcs-tradequeryservice.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/imcs-tradequeryservice.tar docker@192.168.64.4:~/
#docker load < imcs-tradequeryservice.tar

docker save dineshpillai/innovation-mu-streamers > innovation-mu-streamers.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/innovation-mu-streamers.tar docker@192.168.64.4:~/
#docker load < innovation-mu-streamers.tar

docker save dineshpillai/mu-kafka > mu-kafka.tar
#scp -i ~/.minikube/machines/minikube/id_rsa ~/git/innovation/mu-kafka.tar docker@192.168.64.4:~/
#docker load < mu-kafka.tar
