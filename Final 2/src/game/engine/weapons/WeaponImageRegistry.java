package game.engine.weapons;

import java.util.HashMap;
import java.util.Map;

public class WeaponImageRegistry {
    private Map<Integer, String> imageFileNames;

    public WeaponImageRegistry() {
        imageFileNames = new HashMap<>();
    }

    public void registerImage(int weaponCode, String imageFileName) {
        imageFileNames.put(weaponCode, imageFileName);
    }

    public String getImageFileName(int weaponCode) {
        return imageFileNames.get(weaponCode);
    }
}