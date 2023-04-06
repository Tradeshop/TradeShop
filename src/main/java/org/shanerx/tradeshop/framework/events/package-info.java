/*
 *
 *                         Copyright (c) 2016-2023
 *                SparklingComet @ http://shanerx.org
 *               KillerOfPie @ http://killerofpie.github.io
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *                http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NOTICE: All modifications made by others to the source code belong
 *  to the respective contributor. No contributor should be held liable for
 *  any damages of any kind, whether be material or moral, which were
 *  caused by their contribution(s) to the project. See the full License for more information.
 *
 */

/**
 * This package contains all classes belonging to the TradeShop Addon Framework (TSAF),
 * a framework which allows external plugins to listen in on Trade-events, so that they may enhance
 * Trading experience through the addition of external features.
 * To listen for our events, setup a normal Bukkit event listener with the correct event class.
 *
 * More information on the TSAF and the currently available addons on https://tradeshop.github.io.
 *
 * General Notes: do NOT use the constructors of the event classes. They are intended to be listened to, not called.
 * Misuse may cause major problems, and we are not responsible for any damages or loss of data.
 */
package org.shanerx.tradeshop.framework.events;