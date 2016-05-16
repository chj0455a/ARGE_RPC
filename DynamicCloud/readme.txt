#1)
#Se connecter directement aux 3 VMs existantes dans 3 fenêtres (respectivement une vm (VM1) pour le client et une autre vm pour le repartiteur(VM2.1) et le VMManager(VM2.2)) :
ssh ubuntu@195.220.53.33
ssh ubuntu@195.220.53.36
ssh ubuntu@195.220.53.36

#VM2.1 bin
#VM2.1 ./Repartiteur 2000 cloudmip
#--> Le repartiteur se lance

#VM2.2 bin
#VM2.2 ./VMManager localhost 2000

#Lancement du client :
#VM1 ./Client 500 195.220.53.36 2000
# l'adresse est l'adresse privée du répartiteur [jcR]



#2)
# Si les VM n'existent pas, voilà comment les créer pour respectivement VM2 et VM1 à partir de mes images contenant les rogrammes nécessaires et mes propres programmes: 
nova boot --flavor m1.small --image jcImg --nic net-id=c1445469-4640-4c5a-ad86-9c0cb6650cca --security-group default --key-name jckey jcR
nova boot --flavor m1.small --image jcImg --nic net-id=c1445469-4640-4c5a-ad86-9c0cb6650cca --security-group default --key-name jckey jcCa

# Créer des addresses public et les associer :
neutron floatingip-create public
neutron floatingip-create public

nova floating-ip-associate jcCr [IP CREEE]
nova floating-ip-associate jcCa [IP CREEE]

# Puis se connecter de la même facon que spécifié ci dessus mais en mettant à jour les programmes :
# Pour chaque VM, il faut les mettre à jour (t est un alias menant vers le repertoire de travail et m un alias faisant mvn clean install tandis que bin dirige vers les scripts):
t
git pull
# Si le git pull n'est pas autorisé, retourner à la racine es recloner le projet
cd
sudo rm -rf ARGE_RPC/
git clone https://github.com/chj0455a/ARGE_RPC.git
t

# Puis dans tous les cas, recompiler
m




#3)
# Si les images n'existent pas, il faut installer les paquets nécessaire et installer mes programmes :
# Créer une image de base
glance image-create --name default --disk-format qcow2 --container-format=bare --file /nfs/home/gdacosta/ubuntu-15.10-server-cloudimg-amd64-disk1.img

# Créer les différentes VM comme spécifier ci dessus mais en utilisant l'image default au lieu de jcImg

#Pour chaque vm:
sudo apt-get update
sudo apt-get --assume-yes update
sudo apt-get --assume-yes install openjdk-8-jdk
git clone https://github.com/chj0455a/ARGE_RPC.git
sudo apt-get --assume-yes install maven2
sudo apt-get --assume-yes remove openjdk-7-*
cd ARGE_RPC/DynamicCloud/
mvn clean install
cd target/appassembler/bin/


sudo vim ~/.bash_aliases
# par confort, ajouter les alias
alias t="cd ~/ARGE_RPC/DynamicCloud/"
alias bin="cd ~/ARGE_RPC/DynamicCloud/target/appassembler/bin/"
alias src="cd ~/ARGE_RPC/DynamicCloud/src/main/java/m2dl/arge/xmlrpc/"
alias m="mvn clean install"
source ~/.bashrc

# Pour la VM du calculateur, installer les paquets nécessaires pour utiliser Sigar:
sudo apt-get installlibhyperic-sigar-java
# Pour la VM du calculateur, il faut également installer un script pour lancer le programme au démarrage:
sudo vim /etc/init.d/maj_arge.sh
# Editer le fichier:
cd /home/ubuntu/ARGE_RPC/DynamicCloud/target/appassembler/bin/
./Calculateur 2012 &
# enregistrer et quitter : ":wq"
# Rendre le script executable au demarrage : 
sudo chmod 755 /etc/init.d/maj_arge.sh
sudo chown root:root /etc/init.d/maj_arge.sh
sudo update-rc.d maj_arge.sh defaults

# Pour que le programm puisse lancer des VM calculateur, il faut depuis cloudmip faire :
# depuis ensXX@frontal
nova image-create jcCa jcWNimg

# Configurations finies