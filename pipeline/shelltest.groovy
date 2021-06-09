node{
 stage('SCM Checkout'){
    checkout scm
	}
 stage('shell stage'){
   
 dir('build'){
 
   sh('chmod +x notify-git.sh')
   sh('./notify-git.sh \"shashanp\" \"c2227ae0a98cc911d5c89007842d4a31a8ca743d\" \"https://sqbu-github.cisco.com\" \"shashanp\" \"test-trigger-project\" 2 \"this works i guess\"')
 
 }
 }
} 
 
   




