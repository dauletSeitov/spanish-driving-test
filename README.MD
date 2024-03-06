to build docker image: 

docker build -t driver-image .

to save image as archive 
docker save -o driver-image.tar driver-image:latest

to copy archive to server scp -i ssh-key.key driver-image.tar user@host:/home/ubuntu/driver-image

connect to server ssh -i /ssh-key.key user@host

to load archive 
docker load -i driver-image.tar

to copy file to server scp -i ssh-key.key docker-compose.yml user@host:/home/ubuntu/driver-image

to run docker-compose up -d
