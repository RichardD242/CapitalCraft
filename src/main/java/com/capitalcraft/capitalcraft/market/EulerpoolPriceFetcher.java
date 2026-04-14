package com.capitalcraft.capitalcraft.market;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;

public final class EulerpoolPriceFetcher {

    private EulerpoolPriceFetcher() {
    }

    public static OptionalInt fetchUsdScaled(String symbol) {
        OptionalInt pythonResult = runPython("python", symbol);
        if (pythonResult.isPresent()) {
            return pythonResult;
        }
        return runPython("py", symbol);
    }

    private static OptionalInt runPython(String launcher, String symbol) {
        try {
            String script = "import sys\n"
                    + "try:\n"
                    + " import eulerpool\n"
                    + "except Exception:\n"
                    + " print('')\n"
                    + " sys.exit(0)\n"
                    + "s=sys.argv[1]\n"
                    + "price=None\n"
                    + "def pick(obj):\n"
                    + " global price\n"
                    + " keys=['price','close','last','regularMarketPrice','marketPrice','sharePrice']\n"
                    + " for k in keys:\n"
                    + "  v=None\n"
                    + "  try:\n"
                    + "   v=getattr(obj,k)\n"
                    + "  except Exception:\n"
                    + "   pass\n"
                    + "  if v is None and isinstance(obj,dict):\n"
                    + "   v=obj.get(k)\n"
                    + "  if v is not None:\n"
                    + "   try:\n"
                    + "    price=float(v)\n"
                    + "    return\n"
                    + "   except Exception:\n"
                    + "    pass\n"
                    + "for fn in [lambda: eulerpool.equity.quote(s), lambda: eulerpool.equity.price(s), lambda: eulerpool.equity.profile(s)]:\n"
                    + " try:\n"
                    + "  data=fn()\n"
                    + "  pick(data)\n"
                    + " except Exception:\n"
                    + "  pass\n"
                    + " if price is not None:\n"
                    + "  break\n"
                    + "if price is None:\n"
                    + " print('')\n"
                    + "else:\n"
                    + " print(int(round(price*100)))\n";

            Process process = new ProcessBuilder(launcher, "-c", script, symbol)
                    .redirectErrorStream(true)
                    .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                process.waitFor();
                if (line == null || line.isBlank()) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(Integer.parseInt(line.trim()));
            }
        } catch (Exception ignored) {
            return OptionalInt.empty();
        }
    }
}
