module.exports = function(grunt) {
	var fs = require("fs");
	var path = require("path");

	var getAbsoluteDir = function(dir) {
		return path.isAbsolute(dir) ? dir : path.resolve(process.cwd(), dir);
	};

	var globalConfig = {
		lessFile : grunt.option("lessFile"),
		resourceDirectory : getAbsoluteDir(grunt.option("resourceDirectory")),
		targetDirectory : getAbsoluteDir(grunt.option("targetDirectory")),
		cssDirectory : getAbsoluteDir(grunt.option("cssDirectory")),
		assetsDirectory : getAbsoluteDir(grunt.option("assetsDirectory")),
	};

	grunt.initConfig({
		globalConfig : globalConfig,
		pkg : grunt.file.readJSON("package.json"),
		bootstrap : grunt.file.readJSON("node_modules/bootstrap/package.json"),
		copy : {
			deps : {
				files : [
						{
							expand : true,
							cwd : "./node_modules",
							dest : "<%=globalConfig.assetsDirectory%>/angular/js",
							flatten : true,
							src : [ "./angular/*.min.*", "./angular-translate/dist/*.min.*", "./angular-translate-loader-partial/*.min.*",
									"./angular-modal-service/dst/*.min.*" ]
						}, {
							expand : true,
							cwd : "./node_modules/bootstrap-fileinput",
							dest : "<%=globalConfig.assetsDirectory%>/bootstrap-fileinput",
							src : [ "./css/**", "./img/**", "./js/*min.js" ]
						}, {
							expand : true,
							cwd : "./node_modules/summernote",
							dest : "<%=globalConfig.assetsDirectory%>/summernote",
							src : [ "./lang/**", ]
						}, {
							expand : true,
							cwd : "./node_modules/summernote",
							dest : "<%=globalConfig.assetsDirectory%>/summernote",
							flatten : true,
							src : [ "./dist/*.min.*", "./dist/*.css" ]
						}, {
							expand : true,
							cwd : "./node_modules/jquery-sortable",
							dest : "<%=globalConfig.assetsDirectory%>/jquery/plugins",
							flatten : true,
							src : [ "./source/js/*min.js" ]
						}, {
							expand : true,
							cwd : "./node_modules/video.js/dist",
							dest : "<%=globalConfig.assetsDirectory%>/video.js",
							src : [ "**", "!**/*.zip", "!alt/**", "!examples/**" ]
						}, {
							expand : true,
							cwd : "./node_modules/videojs-contrib-hls",
							dest : "<%=globalConfig.assetsDirectory%>/video.js/plugins",
							flatten : true,
							src : [ "./dist/*.js" ]
						}, {
							expand : true,
							cwd : "./node_modules/videojs-thumbnails",
							dest : "<%=globalConfig.assetsDirectory%>/video.js/plugins",
							flatten : true,
							src : [ "./dist/browser/*.js" ]
						} ]
			}
		},
		imagemin : {
			build : {
				options : {
					optimizationLevel : 5
				},
				files : [ {
					expand : true,
					cwd : "<%=globalConfig.resourceDirectory%>/dbt/images",
					src : [ "**/*.{gif,jpg,png,svg}" ],
					dest : "<%=globalConfig.targetDirectory%>/dbt/images/"
				} ]
			}
		},
		less : {
			build : {
				options : {
					banner : "/*!\n" + " * <%= pkg.name %> v${project.version}\n" + " * Homepage: <%= pkg.homepage %>\n"
							+ " * Copyright 2013-<%= grunt.template.today(\"yyyy\") %> <%= pkg.author %> and others\n"
							+ " * Licensed under <%= pkg.license %>\n" + " * Based on Bootstrap\n" + "*/\n",
					compress : true,
					cleancss : true,
					ieCompat : false,
					sourceMap : false,
					sourceMapURL : "",
					sourceMapFilename : "",
					outputSourceFiles : true,
					modifyVars : {
						"icon-font-path" : "\"../../assets/bootstrap/fonts/\"",
						"fa-font-path" : "\"../../assets/font-awesome/fonts\"",
						"brand-primary" : "#008855",
						"brand-success" : "#5cb85c",
						"brand-warning" : "#f0ad4e",
						"brand-danger" : "#d9534f",
						"brand-info" : "#5bc0de",
						"input-border-focus" : "@brand-primary"
					}
				},
				files : {
					"<%=globalConfig.cssDirectory%>/layout.min.css" : "<%=globalConfig.lessFile%>"
				}
			}
		},
		uglify : {
			build : {
				options : {
					banner : "/*!\n" + " * <%= pkg.name %> v${project.version}\n" + " * Homepage: <%= pkg.homepage %>\n"
							+ " * Copyright 2013-<%= grunt.template.today(\"yyyy\") %> <%= pkg.author %> and others\n"
							+ " * Licensed under <%= pkg.license %>\n*/\n",
					preserveComments : false,
					mangle : false,
					sourceMap : true
				},
				files : [ {
					expand : true,
					cwd : "<%=globalConfig.resourceDirectory%>",
					src : "**/*.js",
					dest : "<%=globalConfig.targetDirectory%>",
					ext : ".min.js"
				} ]
			}
		}
	});

	grunt.loadNpmTasks("grunt-contrib-copy");
	grunt.loadNpmTasks("grunt-contrib-imagemin");
	grunt.loadNpmTasks("grunt-contrib-less");
	grunt.loadNpmTasks("grunt-contrib-uglify");

	grunt.registerTask("default", [ "copy", "imagemin", "less", "uglify" ]);
};
