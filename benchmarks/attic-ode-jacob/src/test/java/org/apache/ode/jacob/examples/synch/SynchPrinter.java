/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.jacob.examples.synch;


import org.apache.ode.jacob.RunnableProcess;
import org.apache.ode.jacob.oo.ReceiveProcess;
import org.apache.ode.jacob.oo.Synch;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;

import static org.apache.ode.jacob.oo.ProcessUtil.receive;
import static org.apache.ode.jacob.Jacob.instance;
import static org.apache.ode.jacob.Jacob.newChannel;
import static org.apache.ode.jacob.Jacob.object;


/**
 * Example JACOB process illustrating the use of {@link SynchPrint}
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class SynchPrinter {

    public static final class SystemPrinter extends RunnableProcess {
        private static final long serialVersionUID = -8516348116865575605L;

        private SynchPrint _self;

        public SystemPrinter(SynchPrint self) {
            _self = self;
        }

        @SuppressWarnings("serial")
        public void run() {
            object(true, new ReceiveProcess().setChannel(_self).setReceiver(new SynchPrint() {
                public Synch print(String msg) {
                    System.out.println(msg);
                    return null; // Synch channel automatically created by JacobVPU
                }
            }));
        }
    }

    public static final class Tester extends RunnableProcess {
        private static final long serialVersionUID = 7899682832271627464L;

        public void run() {
            final SynchPrint p = newChannel(SynchPrint.class);
            instance(new SystemPrinter(p));
            dudeWhoStoleMyCar(p)
                .order("garlic chicken")
                .and().then().order("white rice")
                .and().then().order("wonton soup")
                .and().then().order("fortune cookies")
                .and().then().and().then().and().then().and().then()
                .and().no().andthen();
        }

        public static PrinterProcess dudeWhoStoleMyCar(SynchPrint p) {
            return new PrinterProcess(p);
        }

        public static final class PrinterProcess implements Runnable {
            private final SynchPrint printer;
            final private PrinterProcess prev;
            private PrinterProcess next;
            private String message;

            public PrinterProcess(final SynchPrint p) {
                this(p, null);
            }
            private PrinterProcess(final SynchPrint p, final PrinterProcess prev) {
                printer = p;
                this.prev = prev;
            }
            public PrinterProcess order(String message) {
                this.message = message;
                return this;
            }
            public PrinterProcess and() {
                // noop
                return this;
            }
            public PrinterProcess then() {
                if (message == null) {
                    return this;
                }
                next = new PrinterProcess(printer, this);
                return next;
            }
            public PrinterProcess no() {
                return prev != null ? prev.no() : this;
            }
            public void andthen() {
                run();
            }

            public void run() {
                if (message != null) {
                    object(receive(printer.print(message), new Synch() {
                        private static final long serialVersionUID = 1L;
                        public void ret() {
                            if (next != null) {
                                next.run();
                            }
                        }
                    }));
                }
            }
        }
    }

    public static void main(String args[]) {
        JacobVPU vpu = new JacobVPU();
        vpu.setContext(new ExecutionQueueImpl(null));
        vpu.inject(new Tester());
        while (vpu.execute()) {
            // run
        }
    }
}
