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
package org.apache.myfaces.buildtools.maven2.plugin.builder.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Leonardo Uribe
 * @since 1.0.9
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface JSFFaceletFunction
{
    /**
     * The name of the component in a page (ex: x:mycomp).
     * 
     * @since 1.0.9
     */
    String name() default "";

    /**
     * Short description
     * 
     * @since 1.0.9
     */
    String desc() default "";
    
    /**
     * Long description. By default, it takes what is inside comment area.
     * 
     * @since 1.0.9
     */
    String longDescription() default "";    
    
    /**
     * 
     * 
     * @since 1.0.9
     */
    String signature() default "";
    
    /**
     * 
     * @return
     */
    String declaredSignature() default "";
    
    /**
     * The fully-qualified-name of a concrete class implementing the called method.
     * 
     * @since 1.0.9
     */
    String clazz() default "";
}
