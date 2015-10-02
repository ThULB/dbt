module.exports = function(grunt) {
	// Project configuration.
	grunt.initConfig({
		pkg : grunt.file.readJSON('package.json'),
		clean : {
			build : {
				src : [ "build", "dist" ]
			},
		},
		typescript : {
			base : {
				src : [ '<%= pkg.src %>/typescript/**/*.ts' ],
				dest : 'build/js/<%= pkg.name %>.js',
				options : {
					module : 'commonjs', // or amd, commonjs
					target : 'es3', // or es3
					rootDir : 'src',
					sourceMap : true,
					comments : true,
					ignoreError : false,
					declaration : false
				}
			}
		},
		concat : {
			debug : {
				files : {
					'build/<%= pkg.name %>/chrome/content/<%= pkg.name %>.js' : [ 'build/js/<%= pkg.name %>.js' ]
				},
			}
		},
		uglify : {
			build : {
				options : {
					preserveComments : 'some',
					sourceMap : false
				},
				files : {
					'build/<%= pkg.name %>/chrome/content/<%= pkg.name %>.js' : [ 'build/js/<%= pkg.name %>.js' ]
				}
			}
		},
		less : {
			build : {
				options : {
					paths : [ "less" ],
					cleancss : true
				},
				files : {
					"build/<%= pkg.name %>/chrome/content/css/<%= pkg.name %>.css" : [ '<%= pkg.src %>/less/build.less' ]
				}
			}
		},
		copy : {
			debug : {
				expand : true,
				cwd : 'build/',
				src : '*.map',
				dest : 'build/<%= pkg.name %>/chrome/content/'
			},
			build : {
				expand : true,
				cwd : '<%= pkg.src %>/resources/',
				src : '**',
				dest : 'build/<%= pkg.name %>'
			},
		},
		compress : {
			main : {
				options : {
					archive : 'dist/<%= pkg.name %>.zip'
				},
				files : [ {
					expand : true,
					cwd : 'build/<%= pkg.name %>',
					src : [ '**/*' ],
					dest : ''
				} ]
			}
		},
		watch : {
			scripts : {
				files : [ '<%= pkg.src %>/resources/**/*.xul', '<%= pkg.src %>/typescript/**/*.ts', '<%= pkg.src %>/less/**/*.less' ],
				tasks : [ 'typescript', 'concat', 'less', 'copy:debug', 'copy:build' ],
				options : {
					spawn : false,
				},
			},
		},
	});

	// Build Tasks
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-compress');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-less');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-typescript');

	// Build task(s).
	grunt.registerTask('build', [ 'typescript', 'uglify', 'less', 'copy:build' ]);

	// Default task
	grunt.registerTask('default', [ 'clean', 'build', 'compress' ]);
};