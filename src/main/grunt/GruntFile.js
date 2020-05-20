module.exports = function (grunt) {
	var fs = require("fs");
	var path = require("path");

	var getAbsoluteDir = function (dir) {
		return path.isAbsolute(dir) ? dir : path.resolve(process.cwd(), dir);
	};

	var globalConfig = {
		resourceDirectory: getAbsoluteDir(grunt.option("resourceDirectory")),
		targetDirectory: getAbsoluteDir(grunt.option("targetDirectory")),
		assetsDirectory: getAbsoluteDir(grunt.option("assetsDirectory")),
	};

	grunt.initConfig({
		globalConfig: globalConfig,
		pkg: grunt.file.readJSON("package.json"),
		bootstrap: grunt.file.readJSON("node_modules/bootstrap/package.json"),
		copy: {
			deps: {
				files: [
					{
						expand: true,
						cwd: "./node_modules",
						dest: "<%=globalConfig.assetsDirectory%>/angular/js",
						flatten: true,
						src: ["./angular/*.min.*", "./angular-translate/dist/*.min.*", "./angular-translate-loader-partial/*.min.*",
							"./angular-modal-service/dst/*.min.*"]
					}, {
						expand: true,
						cwd: "./node_modules/bootstrap-fileinput",
						dest: "<%=globalConfig.assetsDirectory%>/bootstrap-fileinput",
						src: ["./css/**", "./img/**", "./js/*min.js", "./themes/fas/*min.js"]
					}, {
						expand: true,
						cwd: "./node_modules/@fortawesome/fontawesome-free",
						dest: "<%=globalConfig.assetsDirectory%>/fontawesome-free",
						src: ["./scss/**", "./webfonts/**"]
					}, {
						expand: true,
						cwd: "./node_modules/summernote/dist",
						dest: "<%=globalConfig.assetsDirectory%>/summernote",
						src: ["./font/**", "./lang/**"]
					}, {
						expand: true,
						cwd: "./node_modules/summernote",
						dest: "<%=globalConfig.assetsDirectory%>/summernote",
						flatten: true,
						src: ["./dist/*.min.*", "./dist/*.css"]
					}, {
						expand: true,
						cwd: "./node_modules/jquery-sortable",
						dest: "<%=globalConfig.assetsDirectory%>/jquery/plugins",
						flatten: true,
						src: ["./source/js/*min.js"]
					}, {
						expand: true,
						cwd: "./node_modules/video.js/dist",
						dest: "<%=globalConfig.assetsDirectory%>/video.js",
						src: ["**", "!**/*.zip", "!alt/**", "!examples/**"]
					}, {
						expand: true,
						cwd: "./node_modules/videojs-share/dist",
						dest: "<%=globalConfig.assetsDirectory%>/videojs-share",
						src: ["**"]
					}, {
						expand: true,
						cwd: "./node_modules/node-waves",
						dest: "<%=globalConfig.assetsDirectory%>/waves",
						flatten: true,
						src: ["./dist/*.min.*", "./dist/*.css"]
					}]
			}
		},
		imagemin: {
			build: {
				options: {
					optimizationLevel: 5
				},
				files: [{
					expand: true,
					cwd: "<%=globalConfig.resourceDirectory%>/dbt/images",
					src: ["**/*.{gif,jpg,png,svg}"],
					dest: "<%=globalConfig.targetDirectory%>/dbt/images/"
				}]
			}
		},
		uglify: {
			build: {
				options: {
					banner: "/*!\n" + " * <%= pkg.name %> v${project.version}\n" + " * Homepage: <%= pkg.homepage %>\n"
						+ " * Copyright 2013-<%= grunt.template.today(\"yyyy\") %> <%= pkg.author %> and others\n"
						+ " * Licensed under <%= pkg.license %>\n*/\n",
					preserveComments: false,
					mangle: false,
					sourceMap: true
				},
				files: [{
					expand: true,
					cwd: "<%=globalConfig.resourceDirectory%>",
					src: "**/*.js",
					dest: "<%=globalConfig.targetDirectory%>",
					ext: ".min.js"
				}]
			}
		}
	});

	grunt.loadNpmTasks("grunt-contrib-copy");
	grunt.loadNpmTasks("grunt-contrib-imagemin");
	grunt.loadNpmTasks("grunt-contrib-uglify");

	grunt.registerTask("default", ["copy", "imagemin", "uglify"]);
};
