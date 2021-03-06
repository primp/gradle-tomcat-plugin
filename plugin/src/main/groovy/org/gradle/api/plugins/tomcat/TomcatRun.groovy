/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.plugins.tomcat

import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Deploys an exploded web application to an embedded Tomcat web container. Does not require that the web application
 * be assembled into a war, saving time during the development cycle.
 *
 * @author Benjamin Muschko
 */
class TomcatRun extends AbstractTomcatRunTask {
    static final Logger LOGGER = LoggerFactory.getLogger(TomcatRun.class)
    @InputFiles FileCollection webAppClasspath
    @InputDirectory File webAppSourceDirectory

    @Override
    void validateConfiguration() {
        super.validateConfiguration()

        // Check the location of the static content/JSPs etc.
        try {
            if(!getWebAppSourceDirectory() || !getWebAppSourceDirectory().exists()) {
                throw new InvalidUserDataException('Webapp source directory '
                        + (!getWebAppSourceDirectory() ? 'null' : getWebAppSourceDirectory().canonicalPath)
                        + ' does not exist')
            }
            else {
                LOGGER.info "Webapp source directory = ${getWebAppSourceDirectory().canonicalPath}"
            }
        }
        catch(IOException e) {
            throw new InvalidUserDataException('Webapp source directory does not exist', e)
        }

        File defaultConfigFile = new File(getWebAppSourceDirectory(), "/${CONFIG_FILE}")

        // If context.xml wasn't provided check the default location
        if(!getConfigFile() && defaultConfigFile.exists()){
            setResolvedConfigFile(defaultConfigFile.toURI().toURL())
            LOGGER.info "context.xml = ${getResolvedConfigFile().toString()}"
        }
    }

    @Override
    void setWebApplicationContext() {
        getServer().createContext(getFullContextPath(), getWebAppSourceDirectory().canonicalPath)
    }

    @Override
    void configureWebApplication() {
        super.configureWebApplication()

        LOGGER.info "web app loader classpath = ${getWebAppClasspath().asPath}"
      
        getWebAppClasspath().each { file ->
            getServer().context.loader.addRepository(file.toURI().toURL().toString())
        }
    }
}
