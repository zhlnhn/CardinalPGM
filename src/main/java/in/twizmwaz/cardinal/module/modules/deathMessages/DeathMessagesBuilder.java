package in.twizmwaz.cardinal.module.modules.deathMessages;

import in.twizmwaz.cardinal.match.Match;
import in.twizmwaz.cardinal.module.ModuleBuilder;
import in.twizmwaz.cardinal.module.ModuleCollection;

public class DeathMessagesBuilder implements ModuleBuilder {

    @Override
    public ModuleCollection<DeathMessages> load(Match match) {
        return new ModuleCollection<>(new DeathMessages());
    }
}
