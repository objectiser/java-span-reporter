/**
 * Copyright 2017 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package examples;

import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.opentracing.NoopTracerFactory;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.di.LoggerTracerModule;
import io.opentracing.contrib.di.NoopTracerModule;
import io.opentracing.contrib.reporter.LogLevel;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.contrib.reporter.slf4j.Slf4jReporter;
import io.opentracing.contrib.util.MapMaker;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.LinkedHashMap;

public class Sample01 {
    public static void main(String[] args) throws Exception {
        System.out.print("\n>> only OpenTracing api\n");
        // create the logger is longest operation
        //Tracer tracer = provideTracerByGuice();
        Tracer tracer = provideTracerByConstructor();
        for (int i = 0; i < 5; i++){
            System.out.format("\n>> run %d\n", i);
            run0(tracer);
        }
        //delay to send data to backend-tracer
        //Thread.sleep(2000);
        System.out.print("\n>> end\n");
    }

    private static Tracer provideTracerByGuice() throws Exception {
        //Injector injector =  Guice.createInjector(new BraveModule(), new CtxBasicModule());
        //Injector injector =  Guice.createInjector(new HawkularModule(), new LoggerTracerModule());
        Injector injector = Guice.createInjector(new NoopTracerModule(), new LoggerTracerModule());
        return injector.getInstance(Tracer.class);
    }

    private static Tracer provideTracerByConstructor() throws Exception {
        Tracer backend = NoopTracerFactory.create();
        Reporter reporter = new Slf4jReporter(LoggerFactory.getLogger("tracer"), true);
        return new TracerR(backend, reporter);
    }

    private static void run0(Tracer tracer) throws Exception {
      // start a span
      try(Span span0 = tracer.buildSpan("span-0")
              .withTag("description", "top level initial span in the original process")
              .start()) {
          Tags.HTTP_URL.set(span0, "/orders"); //span.setTag(Tags.HTTP_URL.getKey(), "/orders")
          //Tags.HTTP_METHOD.set(span0, "POST");
          //Tags.PEER_SERVICE.set(span0, "OrderManager");
          //Tags.SPAN_KIND.set(span0, Tags.SPAN_KIND_SERVER);
          try(Span span1 = tracer.buildSpan("span-1")
                  .asChildOf(span0)
                  .withTag("description", "the first inner span in the original process")
                  .start()) {

              // do something

              // start another span

              try(Span span2 = tracer.buildSpan("span-2")
                      .asChildOf(span1)
                      .withTag("description", "the second inner span in the original process")
                      .start()) {


                  // do something
                  span2.log("blablabala");
                  span2.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.DEBUG, "k0", "v0", "k1", 42));
                  span2.log(MapMaker.fields(LogLevel.FIELD_NAME, LogLevel.WARN, "k0", "v0", "ex", new Exception("boom !")));

                  // cross process boundary
                  //Map<String, String> map = new java.util.HashMap<>();
                  //tracer.inject(span2.context(), Format.Builtin.HTTP_HEADERS, new TextMapInjectAdapter(map))
                  Thread.currentThread().sleep(10);
                  // request.addHeaders(map);
                  // request.doGet();
              }
          }
      }
  }
}
