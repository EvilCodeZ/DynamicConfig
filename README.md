# DynamicConfig
Repository for Dynamic config file format.

#### Example:
```
{
  first= = "Test",
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
  seventh = (testAttribute = "Test", test2 = [1, 2, 3, 4]) 8
}
```

#### Java example:
```java
public class ConfigExample {
    
    public static void main(String[] args) {
    		// Parse config
    		final BaseValue value = new ConfigParser().parse("{a=\"Test\",b=1}");
    		final MapValue map = (MapValue) value;
    		System.out.println("a = " + map.getString("a"));
    		System.out.println("b = " + map.getNumber("b").intValue());
            
    		// Serialize config
    		boolean prettyPrinting = true;
    		final String config = new ConfigWriter(prettyPrinting).serialize(map);
    		System.out.println(config);
    }
}
```
