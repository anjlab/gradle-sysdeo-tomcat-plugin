/**
 * Copyright 2013 AnjLab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anjlab.gradle

import groovy.xml.XmlUtil

import org.gradle.api.Plugin
import org.gradle.api.Project

class SysdeoEclipseTomcatGeneratorPlugin implements Plugin<Project>
{
    void apply(Project project)
    {
        project.extensions.create("eclipseTomcat", SysdeoEclipseTomcatGeneratorExtension)
        
        project.eclipseTomcat.init(project)
        
        project.eclipse.project.file
        {
            whenMerged { eclipseProject ->
                def nature = 'com.sysdeo.eclipse.tomcat.tomcatnature'
                if (!eclipseProject.natures.find { it.equals( nature ) }) {
                    eclipseProject.natures.add( nature );
                }
                // also make it work the successor, uqbar's version
                nature = 'org.uqbar.eclipse.tomcat.xt.tomcatnature'
                if (!eclipseProject.natures.find { it.equals( nature ) }) {
                    eclipseProject.natures.add( nature );
                }
            }
        }
        
        project.eclipse.classpath.file
        {
            whenMerged
            {
                classpath ->
                
                def root = new XmlParser().parseText(
                '''<?xml version='1.0' encoding='UTF-8'?>
                   <tomcatProjectProperties>
                      <webClassPathEntries />
                   </tomcatProjectProperties>
                ''')
                
                root.webClassPathEntries + {
                    rootDir project.eclipseTomcat.rootDir
                    exportSource project.eclipseTomcat.exportSource
                    reloadable project.eclipseTomcat.reloadable
                    redirectLogger project.eclipseTomcat.redirectLogger
                    updateXml project.eclipseTomcat.updateXml
                    warLocation project.eclipseTomcat.warLocation
                    webPath project.eclipseTomcat.webPath
                    extraInfo project.eclipseTomcat.extraInfo
                }
                
                def extraInfoString = ""
                if (root.extraInfo.size() > 0) {
                    root.extraInfo.get(0).children().each {
                        extraInfoString = extraInfoString + "\n" + XmlUtil.serialize(it).replaceAll('<\\?[^>]+>','')
                    }
                }
                
                root.remove(root.extraInfo)
                root.appendNode("extraInfo", URLEncoder.encode(extraInfoString, "UTF-8"))
                
                List classPathEntries = []
                
                classPathEntries.addAll project.eclipseTomcat.includeClasspath
                
                project.sourceSets.main.runtimeClasspath.minus(project.configurations.providedCompile).each
                {
                    entry ->
                    
                    def skip = project.eclipseTomcat.excludeClasspath.any
                    {
                        entry.absolutePath.contains(it)
                    }
                    
                    if (!skip)
                    {
                        classPathEntries << entry.absolutePath
                    }
                }
                
                classPathEntries.each { entry ->
                    root.webClassPathEntries.get(0).appendNode(
                        "webClassPathEntry", entry.replaceAll("\\\\", "/"))
                }
                
                def tomcatPluginFile = project.file(".tomcatplugin")
                
                tomcatPluginFile.delete()
                tomcatPluginFile << XmlUtil.serialize(root)
            }
        }
    }
}
