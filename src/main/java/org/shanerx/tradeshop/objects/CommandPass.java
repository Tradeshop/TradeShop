/*
 *     Copyright (c) 2016-2017 SparklingComet @ http://shanerx.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: All modifications made by others to the source code belong
 * to the respective contributor. No contributor should be held liable for
 * any damages of any kind, whether be material or moral, which were
 * caused by their contribution(s) to the project. See the full License for more information
 */

package org.shanerx.tradeshop.objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class CommandPass {

	private CommandSender sender;
	private Command cmd;
	private String label;
	private ArrayList<String> args;

	public CommandPass(CommandSender sender, Command cmd, String label, String[] args) {
		this.sender = sender;
		this.cmd = cmd;
		this.label = label;
		this.args = new ArrayList();

		for (String str : args) {
			this.args.add(str);
		}
	}

	public CommandSender getSender() {
		return sender;
	}

	public Command getCmd() {
		return cmd;
	}

	public String getLabel() {
		return label;
	}

	public int argsSize() {
		return args.size();
	}

	public boolean hasArgAt(int index) {
		return index < argsSize();
	}

	public String getArgAt(int index) {
		if (index < argsSize()) {
			return args.get(index);
		} else {
			throw new ArrayIndexOutOfBoundsException("No argument with index at: " + index);
		}
	}

	public ArrayList<String> getArgs() {
		return args;
	}

	public boolean hasArgs() {
		return argsSize() > 0;
	}
}
