package in.twizmwaz.cardinal.module.modules.regions.parsers;

import in.twizmwaz.cardinal.module.modules.regions.RegionParser;
import in.twizmwaz.cardinal.util.Numbers;
import org.bukkit.util.Vector;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CuboidParser extends RegionParser {

    private final Vector min, max;

    public CuboidParser(Element element) {
        super(element.getAttributeValue("name") != null ? element.getAttributeValue("name") : element.getAttributeValue("id"));
        List<String> values = new ArrayList<>();
        values.addAll(Arrays.asList(element.getAttributeValue("min").contains(",") ?
                element.getAttributeValue("min").trim().split(",") : element.getAttributeValue("min").trim().split(" ")));
        values.addAll(Arrays.asList(element.getAttributeValue("max").trim().replaceAll(" ", ",").split(",")));
        for (String string : values) {
            values.set(values.indexOf(string), string.trim());
        }
        this.min = new Vector(Numbers.parseDouble(values.get(0)), Numbers.parseDouble(values.get(1)), Numbers.parseDouble(values.get(2)));
        this.max = new Vector(Numbers.parseDouble(values.get(3)), Numbers.parseDouble(values.get(4)), Numbers.parseDouble(values.get(5)));
    }

    public Vector getMin() {
        return min;
    }

    public Vector getMax() {
        return max;
    }
}
