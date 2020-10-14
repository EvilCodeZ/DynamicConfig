# DynamicConfig
Repository for Dynamic config file format.

#### Example:
```
{
  first = "Test",
  second = 1,
  third = 'a',
  fourth = true,
  fifth = {
    first = 2
  },
  sixth = [
    1,
    "Test",
    'b',
    false
  },
  seventh = (testAttribute = "Test", test2 = [1, 2, 3, 4]) 8,
  eight = null
}
```

#### Java example:
```java
public class ConfigExample {
    
    // This annotation is optional.
    @SerializedFieldName("test")
    private final int testField;
    
    public ConfigExample(int test) {
        this.testField = test;
    }

    public static void main(String[] args) {
        // Parse config
        final BaseValue value = new ConfigParser().parse("{a=\"Test\",b=1}");
        final MapValue map = (MapValue) value;
        System.out.println("a = " + map.getString("a"));
        System.out.println("b = " + map.getNumber("b").intValue());
        
        // Serialize config
        boolean prettyPrinting = true;
        final ConfigWriter writer = new ConfigWriter(prettyPrinting);
        writer.setMapColonSeperator(false);
        writer.setSemicolonSeperator(false);
        final String config = writer.serialize(map);
        System.out.println(config);
        
        // Object serialization
        final ObjectSerializer serializer = new ObjectSerializer();
        // Serialize
        final BaseValue val = serializer.serialize(new ConfigExample(6));
        System.out.println(writer.serialize(val));
        // Deserialize
        final ConfigExample example = serializer.deserialize(val, ConfigExample.class);
        System.out.println(example.testField);
    }
}
```
