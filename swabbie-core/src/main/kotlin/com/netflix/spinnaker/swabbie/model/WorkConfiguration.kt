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

package com.netflix.spinnaker.swabbie.model

import com.netflix.spinnaker.config.Exclusion
import com.netflix.spinnaker.config.NotificationConfiguration

/**
 * @param retention How many days swabbie will wait until starting the deletion process (soft delete, then delete)
 * @param maxAge resources newer than the maxAge in days will be excluded
 */
data class WorkConfiguration(
  val namespace: String,
  val account: Account,
  val location: String,
  val cloudProvider: String,
  val resourceType: String,
  val retention: Int,
  val softDelete: SoftDelete,
  val exclusions: List<Exclusion>,
  val dryRun: Boolean = true,
  val entityTaggingEnabled: Boolean = false,
  val notificationConfiguration: NotificationConfiguration = EmptyNotificationConfiguration(),
  val maxAge: Int = 14,
  val maxItemsProcessedPerCycle: Int = 10,
  val itemsProcessedBatchSize: Int = 5
) {

  fun toLog(): String = "$namespace/$account/$location/$cloudProvider/$resourceType"
}

class EmptyNotificationConfiguration : NotificationConfiguration(
  enabled = false,
  types = mutableListOf(),
  optOutBaseUrl = ""
)

/**
 * @param enabled controls if soft deleting is enabled for this work configuration
 * @param waitingPeriodDays how many days swabbie will wait after soft deleting a resource before deleting it
 */
data class SoftDelete(
  var enabled: Boolean = false,
  var waitingPeriodDays: Int = 2
)
