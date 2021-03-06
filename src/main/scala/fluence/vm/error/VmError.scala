/*
 * Copyright 2019 Fluence Labs Limited
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

package fluence.vm.error

import scala.util.control.NoStackTrace

/**
 * Base trait for errors occurred in Virtual machine.
 *
 * @param message detailed error message
 * @param causedBy caught [[Throwable]], if any
 */
sealed abstract class VmError(val message: String, val causedBy: Option[Throwable])
    extends Throwable(message, causedBy.orNull, true, false) with NoStackTrace

/**
 * Corresponds to errors occurred during VM initialization.
 *
 * @param message detailed error message
 * @param causedBy caught [[Throwable]], if any
 */
case class InitializationError(override val message: String, override val causedBy: Option[Throwable] = None)
    extends VmError(message, causedBy)

/**
 * Corresponds to errors occurred during VM function invocation.
 *
 * @param message detailed error message
 * @param causedBy caught [[Throwable]], if any
 */
case class InvocationError(override val message: String, override val causedBy: Option[Throwable] = None)
    extends VmError(message, causedBy)

/**
 * Corresponds to errors occurred during computing VM state hash.
 *
 * @param message detailed error message
 * @param causedBy caught [[Throwable]], if any
 */
case class StateComputationError(override val message: String, override val causedBy: Option[Throwable] = None)
    extends VmError(message, causedBy)
