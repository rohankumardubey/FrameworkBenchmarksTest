pipeline {
	agent any

    parameters {
        choice(name: 'BUILD_TYPE', choices: [ 'TEST', 'PERFORMANCE' ], description: 'Indicates what type of build')
    }
    
    environment {
        PERFORMANCE_EMAIL = credentials('performance-email')
        RESULTS_EMAIL = credentials('results-email')
        REPLY_TO_EMAIL = credentials('reply-to-email')
    }
    
    triggers {
        parameterizedCron('''
H 3 * * 1 %BUILD_TYPE=TEST
''')
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
		disableConcurrentBuilds()
		timeout(time: 8, unit: 'HOURS')
    }

	tools {
	    maven 'maven-3.6.0'
	    jdk 'jdk15'
	}
	
	stages {
	
		stage('Set build name') {
			steps {
				script {
				    currentBuild.displayName = "#${BUILD_NUMBER} (${params.BUILD_TYPE})"
				}
			}
		}
	
		stage('Check master') {
	        when {
	        	allOf {
	        	    not { branch 'master' }
	        	}
	        }
            steps {
            	script {
            		currentBuild.result = 'ABORTED'
            	}
            	error "Aborting ${params.BUILD_TYPE} as not on master branch"
            }
        }
        
        stage('Checkout FrameworkBenchmarks') {
			when {
				allOf {
    				branch 'master'
				}
			}
			steps {
	        	echo "JAVA_HOME = ${env.JAVA_HOME}"
				sh './checkout_FrameworkBenchmarks.sh'
	        }
        }

        stage('Build FrameworkBenchmarks (OfficeFloor)') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
    				branch 'master'
				}
			}
			steps {
				sh './build_FrameworkBenchmarks.sh'
	        }
        }
	
	    stage('Test') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'TEST' }
    				branch 'master'
				}
			}
	        steps {
				sh 'mvn clean install'
	        }
		    post {
			    always {
					junit allowEmptyResults: true, testResults: 'FrameworkBenchmarks/frameworks/Java/officefloor/src/**/target/surefire-reports/TEST-*.xml'
					junit allowEmptyResults: true, testResults: 'FrameworkBenchmarks/frameworks/Java/officefloor/src/**/target/failsafe-reports/TEST-*.xml'
			    }
		    }
	    } 
	    	    
		stage('Performance') {
			when {
				allOf {
					expression { params.BUILD_TYPE == 'PERFORMANCE' }
					branch 'master'
				}
			}
			steps {
				sh './run_FrameworkBenchmarks.sh'
			}
			post {
			    always {
	            	script {
   						if (currentBuild.result != 'ABORTED') {
							emailext to: "${PERFORMANCE_EMAIL}", replyTo: "${REPLY_TO_EMAIL}", subject: 'Benchmark ' + "${params.BUILD_TYPE}" + ' RESULTS (${BUILD_NUMBER})', attachmentsPattern: 'results.txt, results.zip', body: '''
${PROJECT_NAME} - ${BUILD_NUMBER} - ${BUILD_STATUS}
'''
						}
					}
    			}
			}
		}
	}
	
    post {
   		always {
            script {
   				if (currentBuild.result != 'ABORTED') {
   					if (params.BUILD_TYPE == 'TEST') {
		    			emailext to: "${RESULTS_EMAIL}", replyTo: "${REPLY_TO_EMAIL}", subject: 'Benchmark ' + "${params.BUILD_TYPE}" + ' ${BUILD_STATUS}! (${BUILD_NUMBER})', body: '''
${PROJECT_NAME} - ${BUILD_NUMBER} - ${BUILD_STATUS}

Tests:
Passed: ${TEST_COUNTS,var="pass"}
Failed: ${TEST_COUNTS,var="fail"}
Skipped: ${TEST_COUNTS,var="skip"}
Total: ${TEST_COUNTS,var="total"}

${FAILED_TESTS}


Changes (since last successful build):
${CHANGES_SINCE_LAST_SUCCESS}


Log (last lines):
...
${BUILD_LOG}
'''
					}
				}
			}
		}
	}

}
