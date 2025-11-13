module.exports = function (grunt) {
    const fs = require("fs");
    const path = require("path");
    const sassImpl = require('sass');

	const getAbsoluteDir = function (dir) {
		return path.isAbsolute(dir) ? dir : path.resolve(process.cwd(), dir);
	};

	const globalConfig = {
		resourceDirectory: getAbsoluteDir(grunt.option("resourceDirectory")),
		targetDirectory: getAbsoluteDir(grunt.option("targetDirectory")),
		assetsDirectory: getAbsoluteDir(grunt.option("assetsDirectory")),
        mirAssetsDirectory: getAbsoluteDir(grunt.option("mirAssetsDirectory")),
        sassLoadPath: [getAbsoluteDir(grunt.option("assetsDirectory") + path.sep + '..' + path.sep),
            getAbsoluteDir(grunt.option("mirAssetsDirectory"))]
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
        sass: {
            options: {
                implementation: sassImpl,
                sourceMap: true,
                sourceMapIncludeSources: true,
                outputStyle: 'expanded', // lesbar, wird gleich von cssnano minifiziert
                verbose: true,
                loadPaths: globalConfig.sassLoadPath
            },
            dist: {
                files: [{
                    expand: true,
                    cwd: "<%=globalConfig.resourceDirectory%>/scss",
                    src: ["*.scss"],
                    dest: "<%=globalConfig.targetDirectory%>/dbt/css/",
                    ext: ".css"
                }]
            }
        },
        postcss: {
            options: {
                syntax: require('postcss-scss'),
                map: {
                    inline: false
                },
                processors: [
                    require('@csstools/postcss-sass')({
                        sass: sassImpl,
                        includePaths: globalConfig.sassLoadPath
                    }),
                    require('autoprefixer')(),
                    require('postcss-normalize-whitespace')(),
                    require('postcss-convert-values')(),

                    require('postcss-combine-media-query')(),
                    require('postcss-combine-duplicated-selectors')(),
                    require('cssnano')({
                        preset: 'advanced'
                    })
                ]
            },
            files: {
                expand: true,
                cwd: "<%=globalConfig.resourceDirectory%>/scss",
                src: ["*.scss"],
                dest: "<%=globalConfig.targetDirectory%>/dbt/css/",
                ext: ".min.css"
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
    grunt.loadNpmTasks('grunt-sass');
    grunt.loadNpmTasks('@lodder/grunt-postcss');

	grunt.registerTask("default", ["copy", "postcss", "imagemin", "uglify"]);
};
