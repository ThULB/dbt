module.exports = function(grunt) {
	// Project configuration.
	grunt.initConfig({
		pkg : grunt.file.readJSON('package.json'),
		clean : {
			build : {
				src : [ "build" ]
			},
		},
		bower : {
			install : {
				options : {
					targetDir : 'build/lib',
					layout : 'byComponent'
				}
			}
		},
		tsd : {
			refresh : {
				options : {
					command : 'reinstall',
					latest : true,
					overwrite : true,
					save : true,
					config : 'tsd.json',
				}
			}
		},
		typescript : {
			base : {
				src : [ 'typescript/**/*.ts' ],
				dest : 'build/js/<%= pkg.name %>.js',
				options : {
					module : 'commonjs', // or amd, commonjs
					target : 'es3', // or es3
					basePath : 'src',
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
		/*
		 * less : { build : { options : { paths : [ "less" ], cleancss : true },
		 * files : { "build/www/css/<%= pkg.name %>.css" : "src/main/less/<%=
		 * pkg.name %>.less" } } },
		 */
		copy : {
			build : {
				expand : true,
				cwd : 'resources/',
				src : '**',
				dest : 'build/<%= pkg.name %>'
			},
		},
		watch : {
			scripts : {
				files : [ 'typescript/**/*.ts', 'less/**/*.less' ],
				tasks : [ 'typescript', 'concat'/* , 'less' */, 'copy:build' ],
				options : {
					spawn : false,
				},
			},
		},
	});

	// Build Tasks
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	// grunt.loadNpmTasks('grunt-contrib-less');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-bower-task');
	grunt.loadNpmTasks('grunt-tsd');
	grunt.loadNpmTasks('grunt-typescript');

	// Build task(s).
	grunt.registerTask('build', [ 'bower', 'typescript', 'uglify', 'copy:build' ]);
};