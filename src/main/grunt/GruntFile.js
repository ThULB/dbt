module.exports = function(grunt) {
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-less');
	grunt.loadNpmTasks('grunt-replace');
	var fs = require('fs');
	var globalConfig = {
		lessFile : grunt.option('lessFile'),
		lessDirectory : function() {
			var lessFile = grunt.config('globalConfig.lessFile');
			return lessFile.substring(0, Math.max(lessFile.lastIndexOf("/"), lessFile.lastIndexOf("\\")));
		},
		targetDirectory : grunt.option('targetDirectory'),
		assetsDirectory : grunt.option('assetsDirectory'),
		lastModified : new Date(0)
	};
	var dirLastModified = function(dir, date) {
		var src = grunt.file.expand(dir + '/**/*.less');
		var modified = [];
		src.forEach(function(file, index) {
			var stat = fs.statSync(file);
			modified[index] = stat.mtime;
		});
		return new Date(Math.max.apply(Math, modified));
	};
	var createFileIfNotExist = function(filepath) {
		if (!grunt.file.exists(filepath)) {
			grunt.file.write(filepath);
		}
	};
	var needRebuild = function(dest) {
		var destModified = fs.existsSync(dest) ? fs.statSync(dest).mtime : new Date(0);
		var srcModified = grunt.config('globalConfig.lastModified');
		return srcModified.getTime() > destModified.getTime();
	}

	grunt.initConfig({
		globalConfig : globalConfig,
		pkg : grunt.file.readJSON('package.json'),
		bootstrap : grunt.file.readJSON('bower_components/bootstrap/package.json'),
		banner : '/*!\n' + ' * <%= pkg.name %> v${project.version}\n' + ' * Homepage: <%= pkg.homepage %>\n'
				+ ' * Copyright 2013-<%= grunt.template.today("yyyy") %> <%= pkg.author %> and others\n' + ' * Licensed under <%= pkg.license %>\n'
				+ ' * Based on Bootstrap\n' + '*/\n',
		replace : {
			dist : {
				options : {
					patterns : [
					// resolve css from google fonts on build time
					{
						match : /@import url\("\/\/fonts/,
						replacement : '@import (inline) url("http://fonts'
					} ]
				}
			}
		},
		copy : {
			main : {
				files : [ {
					expand : true,
					cwd : 'bower_components/bootstrap/dist/',
					src : [ 'fonts/**', 'js/**' ],
					dest : '<%=globalConfig.assetsDirectory%>/bootstrap/'
				}, {
					expand : true,
					cwd : 'bower_components/jquery/dist/',
					src : [ '**' ],
					dest : '<%=globalConfig.assetsDirectory%>/jquery/'
				}, {
					expand : true,
					cwd : 'bower_components/font-awesome/',
					src : [ 'fonts/**' ],
					dest : '<%=globalConfig.assetsDirectory%>/font-awesome/'
				}, {
					expand : true,
					cwd : 'bower_components/bootstrap3-wysihtml5-bower/dist/',
					src : [ '**' ],
					dest : '<%=globalConfig.assetsDirectory%>/bootstrap3-wysihtml5/'
				}, {
					expand : true,
					cwd : 'bower_components/wysihtml5x/dist/',
					src : [ '**' ],
					dest : '<%=globalConfig.assetsDirectory%>/wysihtml5x/'
				}, {
					expand : true,
					cwd : 'bower_components/handlebars/',
					src : [ 'handlebars.js', 'handlebars.min.js' ],
					dest : '<%=globalConfig.assetsDirectory%>/handlebars/'
				} ]
			}
		},
		concat : {
			options : {
				banner : '<%= banner %>',
				stripBanners : false
			},
			dist : {
				src : [],
				dest : ''
			}
		},
		less : {
			dist : {
				options : {
					compress : false,
					cleancss : false,
					ieCompat : false,
					sourceMap : true,
					sourceMapURL : "",
					sourceMapFilename : "",
					outputSourceFiles : true,
					modifyVars : {
						"icon-font-path" : "'../assets/bootstrap/fonts/'",
						"fa-font-path" : "'../assets/font-awesome/fonts'",
						"brand-primary" : "#008855",
						"brand-success" : "#5cb85c",
						"brand-warning" : "#f0ad4e",
						"brand-danger" : "#d9534f",
						"brand-info" : "#5bc0de",
						"input-border-focus" : "@brand-primary"
					}
				},
				files : {}
			}
		}
	});
	grunt.registerTask('none', function() {
	});
	grunt.registerTask('build', 'build a regular theme', function(theme, compress) {
		var target = grunt.config('globalConfig.targetDirectory') + '/' + theme + '.css';
		if (needRebuild(target)) {
			var compress = compress == undefined ? true : compress;

			var concatSrc;
			var concatDest;
			var lessDest;
			var lessSrc;
			var files = {};
			var dist = {};
			lessDest = 'build.css';
			lessSrc = [ '<%=globalConfig.lessFile%>' ];
			concatSrc = lessDest;
			concatDest = '<%=globalConfig.targetDirectory%>/' + theme + '.css';
			dist = {
				src : concatSrc,
				dest : concatDest
			};
			grunt.config('concat.dist', dist);

			files = {};
			files[lessDest] = lessSrc;
			grunt.config('less.dist.files', files);
			grunt.config('less.dist.options.compress', false);
			grunt.config('less.dist.options.cleancss', false);
			grunt.config('less.dist.options.sourceMap', true);
			grunt.config('less.dist.options.sourceMapURL', theme + '.css.map');
			grunt.config('less.dist.options.sourceMapFilename', '<%=globalConfig.targetDirectory%>/' + theme + '.css.map');
			grunt.log.writeln('compiling file ' + lessSrc + ' ==> ' + lessDest);

			grunt.task.run([ 'less:dist', 'concat',
					compress ? 'compress:' + concatDest + ':' + '<%=globalConfig.targetDirectory%>/' + theme + '.min.css' : 'none' ]);
		} else {
			grunt.log.writeln('do not need to rebuild ' + target);
		}
	});

	grunt.registerTask('compress', 'compress a generic css', function(fileSrc, fileDst) {
		var files = {};
		files[fileDst] = fileSrc;
		grunt.log.writeln('compressing file ' + fileSrc);

		grunt.config('less.dist.files', files);
		grunt.config('less.dist.options.compress', true);
		grunt.config('less.dist.options.cleancss', true);
		grunt.config('less.dist.options.sourceMap', false);
		grunt.task.run([ 'less:dist' ]);
	});

	grunt.registerTask('default', 'build a theme', function() {
		grunt.log.writeln('less directory: ' + grunt.config('globalConfig').lessDirectory());
		grunt.task.run('copy');
		grunt.task.run('replace');
		grunt.config('globalConfig.lastModified', new Date(Math.max(dirLastModified(grunt.config('globalConfig').lessDirectory()),
				dirLastModified('bower_components'))));
		grunt.task.run('build:layout');
	});

}