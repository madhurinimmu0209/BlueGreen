output "ec2instance_ip" {
  value = "${aws_instance.web-server-instance.public_ip}"
}