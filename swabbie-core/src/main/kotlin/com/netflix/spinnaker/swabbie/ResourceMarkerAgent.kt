/*
 * Copyright 2018 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.swabbie

import com.netflix.discovery.DiscoveryClient
import com.netflix.spinnaker.swabbie.handlers.ResourceHandler
import com.netflix.spinnaker.swabbie.scheduler.WorkProducer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executor

@Component
class ResourceMarkerAgent(
  private val executor: Executor,
  private val workProducer: WorkProducer,
  @Autowired(required = false) private val discoveryClient: DiscoveryClient?,
  private val resourceHandlers: List<ResourceHandler>
): DiscoverySupport(discoveryClient = discoveryClient) {
  private val log: Logger = LoggerFactory.getLogger(javaClass)

  @Scheduled(fixedDelayString = "\${resource.markers.interval:120000}")
  fun execute() {
    if (enabled()) {
      try {
        log.info("Swabbie markers started...")
        workProducer.createWork().let { work ->
          work.forEach { w ->
            workProducer.remove(w)
            resourceHandlers.find { handler -> handler.handles(w.resourceType, w.cloudProvider) }.let { handler ->
              if (handler == null) {
                throw IllegalStateException(
                  String.format("No Suitable handler found for %s", w)
                )
              } else {
                executor.execute {
                  handler.mark(w)
                }
              }
            }
          }
        }
      } catch (e: Exception) {
        log.error("failed", e)
      }
    }
  }
}
