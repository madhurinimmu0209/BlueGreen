
provider "aws" {
  region = "us-east-2"
}


resource "aws_instance" "web-server-instance"{
  ami           = "ami-01aab85a5e4a5a0fe"
  instance_type = "t2.micro"
  key_name      = "jenkins"
  tags = {
    name = "webserver"
  }
}

#resource "tls_private_key" "example" {
#  algorithm = "RSA"
#  rsa_bits  = 4096
#}

#resource "aws_key_pair" "generated_key" {
#  key_name   = "httpd_key"
#  public_key = tls_private_key.example.public_key_openssh
#}


