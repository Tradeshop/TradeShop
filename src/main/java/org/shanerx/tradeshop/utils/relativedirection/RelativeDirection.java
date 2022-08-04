/*
 *
 *                         Copyright (c) 2016-2019
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

package org.shanerx.tradeshop.utils.relativedirection;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;

import java.util.ArrayList;
import java.util.List;

public class RelativeDirection {

    private final List<RelativeCardinals> cardinals = new ArrayList<>();

    public RelativeDirection(RelativeCardinals... cardinals) {
        for (RelativeCardinals cardinal : cardinals) {
            if (cardinal == null)
                cardinal = RelativeCardinals.SELF;

            if (!this.cardinals.contains(cardinal)) {
                this.cardinals.add(cardinal);
            }
        }
    }

    public static RelativeDirection valueOf(String str) {
        String[] cardinals = str.split("[+]");

        if (cardinals.length == 0) return new RelativeDirection(RelativeCardinals.SELF);

        RelativeCardinals[] processedCardinals = new RelativeCardinals[cardinals.length];

        for (int i = 0; i < cardinals.length; i++) {
            processedCardinals[i] = RelativeCardinals.valueOf(cardinals[i]);
        }

        return new RelativeDirection(processedCardinals);
    }

    public static BlockFace getDirection(BlockData blockData) {
        if (blockData instanceof Rotatable) return ((Rotatable) blockData).getRotation();
        else if (blockData instanceof Directional) return ((Directional) blockData).getFacing();
        else return BlockFace.SELF;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (RelativeCardinals cardinal : cardinals) {
            sb.append(cardinal.name()).append("+");
        }

        return sb.substring(0, sb.length() - 1);
    }

    public LocationOffset getTranslatedDirection(BlockFace directionFacing) {
        LocationOffset sum = new LocationOffset(0, 0, 0);

        for (RelativeCardinals cardinal : cardinals) {
            sum = sum.add(cardinal.getTranslation(directionFacing));
        }

        return sum;
    }

}
