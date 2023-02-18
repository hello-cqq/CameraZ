/*
 * Copyright 2023 AhahahQ
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * File: PublishPlugin
 * Description:
 * Version: 1.0
 * Date : 2023/2/18
 * Author: hey.cqq@gmail.com
 *
 * ---------------------Revision History: ---------------------
 *  <author>           <data>          <version >       <desc>
 *  AhahahQ            2023/2/18         1.0         build this module
*/
package com.ahahahq.gradle

import com.android.build.api.artifact.SingleArtifact
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import java.net.URI

class PublishPlugin : Plugin<Project> {
    companion object {
        private const val PLUGIN_MAVEN_PUBLISH = "maven-publish"
        private const val SOURCE_JAR = "sourceJar"
        private const val SNAPSHOT_MAVEN_NAME = "release"
        private const val SNAPSHOT_MAVEN_URL =
            "https://s01.oss.sonatype.org/content/repositories/releases/"
    }

    override fun apply(target: Project) {
        val extension =
            target.extensions.create(PublishExtension.EXTENSION_NAME, PublishExtension::class.java)
        target.afterEvaluate {
            extension.groupId ?: return@afterEvaluate
            createPublishTask(target, extension)
        }
    }

    private fun createPublishTask(target: Project, extension: PublishExtension) {
        if (target.plugins.hasPlugin(PLUGIN_MAVEN_PUBLISH)) {
            return
        }
        target.plugins.apply(PLUGIN_MAVEN_PUBLISH)
        val libExtension = target.extensions.getByType(LibraryExtension::class.java)
        val publishing = target.extensions.getByType(PublishingExtension::class.java)
        if (extension.needSource) {
            target.tasks.create(SOURCE_JAR, Jar::class.java) {
                it.group = "publishing"
                it.from(libExtension.sourceSets.getByName("main").java.srcDirs)
                it.archiveClassifier.set("sources")
            }
        }
        publishing.repositories { r ->
            r.maven {
                it.isAllowInsecureProtocol = true
                it.name = extension.mavenName ?: SNAPSHOT_MAVEN_NAME
                it.url = URI.create(extension.mavenUrl ?: SNAPSHOT_MAVEN_URL)
                it.credentials.username = extension.userName
                it.credentials.password = extension.password
            }
        }
        publishing.publications { p ->
            libExtension.libraryVariants.forEach { v ->
                if (v.name.lowercase().endsWith("release")) {
                    p.create(v.name, MavenPublication::class.java) { m ->
                        m.groupId = extension.groupId
                        extension.artifactId?.let { m.artifactId = it }
                        extension.version?.let { m.version = it }
                        m.from(target.components.findByName(v.name))
                        if (extension.needSource) {
                            m.artifact(target.tasks.findByPath(SOURCE_JAR))
                        }
                        m.pom { pom ->
                            pom.name.set(m.artifactId)
                            extension.description?.let { pom.description.set(it) }
                            extension.openUrl?.let { pom.url.set(it) }
                        }
                    }
                }
            }
        }
    }
}