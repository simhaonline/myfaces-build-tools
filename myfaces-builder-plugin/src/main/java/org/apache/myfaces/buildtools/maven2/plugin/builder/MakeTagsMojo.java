/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.myfaces.buildtools.maven2.plugin.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.myfaces.buildtools.maven2.plugin.builder.model.ComponentMeta;
import org.apache.myfaces.buildtools.maven2.plugin.builder.model.Model;
import org.apache.myfaces.buildtools.maven2.plugin.builder.utils.BuildException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 * Maven goal to generate java source code for Component tag classes.
 * 
 * @version $Id$
 * @requiresDependencyResolution compile
 * @goal make-tags
 * @phase generate-sources
 */
public class MakeTagsMojo extends AbstractMojo
{
    final Logger log = Logger.getLogger(MakeTagsMojo.class.getName());

    /**
     * Injected Maven project.
     * 
     * @parameter expression="${project}"
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${project.build.directory}"
     * @readonly
     */
    private File buildDirectory;

    /**
     * Injected name of file generated by earlier run of BuildMetaDataMojo goal.
     * 
     * @parameter
     */
    private String metadataFile = "classes/META-INF/myfaces-metadata.xml";

    /**
     * @parameter expression="src/main/resources/META-INF"
     */
    private File templateSourceDirectory;
    
    /**
     * @parameter 
     */
    private String templateTagName;

    /**
     * @parameter expression="${project.build.directory}/maven-faces-plugin/main/java"
     * @required
     */
    private File generatedSourceDirectory;

    /**
     * @parameter
     * @required
     */
    private String packageContains;

    /**
     * @parameter
     * @required
     */
    private String typePrefix;

    /**
     * @parameter
     */
    private boolean force;

    /**
     * @parameter
     */
    private boolean suppressListenerMethods;

    /**
     * @parameter
     */
    private String jsfVersion;
    
    /**
     * Execute the Mojo.
     */
    public void execute() throws MojoExecutionException
    {
        // This command makes Maven compile the generated source:
        // getProject().addCompileSourceRoot( absoluteGeneratedPath.getPath() );
        
        try
        {            
            Model model = IOUtils.loadModel(new File(buildDirectory,
                    metadataFile));
            List models = IOUtils.getModelsFromArtifacts(project);
            new Flattener(model).flatten();
            generateComponents(model);
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error generating components", e);
        }
        catch (BuildException e)
        {
            throw new MojoExecutionException("Error generating components", e);
        }
    }
    
    private VelocityEngine initVelocity() throws MojoExecutionException
    {

        Properties p = new Properties();
            
        p.setProperty( "resource.loader", "file, class" );
        p.setProperty( "velocimacro.library", "tagClassMacros11.vm");
        p.setProperty( "file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        p.setProperty( "file.resource.loader.path", templateSourceDirectory.getPath());
        p.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
        p.setProperty( "class.resource.loader.path", "src/main/resources/META-INF");
        p.setProperty( "velocimacro.permissions.allow.inline","true");
        p.setProperty( "velocimacro.permissions.allow.inline.local.scope", "true");

        File template = new File(templateSourceDirectory, _getTemplateTagName());
        
        if (template.exists())
        {
            log.info("Using template from file loader: "+template.getPath());
        }
        else
        {
            log.info("Using template from class loader: src/main/resources/META-INF/"+_getTemplateTagName());
        }
        
        VelocityEngine velocityEngine = new VelocityEngine();
        
        try
        {
            velocityEngine.init(p);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error creating VelocityEngine", e);
        }
        
        return velocityEngine;
    }

    /**
     * Generates parsed components.
     */
    private void generateComponents(Model model) throws IOException,
            MojoExecutionException
    {
        VelocityEngine velocityEngine = initVelocity();
        
        for (Iterator it = model.getComponents().iterator(); it.hasNext();)
        {
            ComponentMeta component = (ComponentMeta) it.next();
            
            if (component.getTagClass() != null)
            {
                log.info("Generating tag class:"+component.getTagClass());
                _generateComponent(velocityEngine, component);
            }
        }
        //throw new MojoExecutionException("stopping..");
    }

    /**
     * Generates a parsed component.
     * 
     * @param component
     *            the parsed component metadata
     */
    private void _generateComponent(VelocityEngine velocityEngine, ComponentMeta component)
            throws MojoExecutionException
    {

        Context context = new VelocityContext();
        context.put("component", component);

        Writer writer = null;
        File outFile = null;

        try
        {
            outFile = new File(generatedSourceDirectory, StringUtils.replace(
                    component.getTagClass(), ".", "/")+".java");

            if ( !outFile.getParentFile().exists() )
            {
                outFile.getParentFile().mkdirs();
            }

            writer = new OutputStreamWriter(new FileOutputStream(outFile));

            Template template = velocityEngine.getTemplate(_getTemplateTagName());
                        
            template.merge(context, writer);

            writer.flush();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException(
                    "Error merging velocity templates: " + e.getMessage(), e);
        }
        finally
        {
            IOUtil.close(writer);
            writer = null;
        }
    }
    
    private String _getTemplateTagName(){
        if (templateTagName != null){
            if (_is12()){
                return "tagClass12.vm";
            }else{
                return "tagClass11.vm";
            }
        }
        return "tagClass11.vm";
    }

    private boolean _is12()
    {
        return "1.2".equals(jsfVersion) || "12".equals(jsfVersion);
    }

}