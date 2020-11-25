/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.cave.repository.service.command.completers;

import org.apache.karaf.cave.repository.Repository;
import org.apache.karaf.cave.repository.RepositoryService;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;

import java.util.List;

@Service
public class RepositoryNameCompleter implements Completer {

    @Reference
    private RepositoryService repositoryService;

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> list) {
        StringsCompleter delegate = new StringsCompleter();
        for (Repository repository : repositoryService.repositories()) {
            delegate.getStrings().add(repository.getName());
        }
        return delegate.complete(session, commandLine, list);
    }
}
