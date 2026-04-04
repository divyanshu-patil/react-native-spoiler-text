<div align="center">

<br />

<img src="https://img.shields.io/badge/react--native-%3E%3D0.73-blue?style=for-the-badge&logo=react" alt="React Native" />
<img src="https://img.shields.io/npm/v/react-native-spoiler-text?style=for-the-badge&color=crimson" alt="npm version" />
<img src="https://img.shields.io/npm/dm/react-native-spoiler-text?style=for-the-badge&color=orange" alt="npm downloads" />
<img src="https://img.shields.io/badge/platforms-iOS%20%7C%20Android-lightgrey?style=for-the-badge" alt="Platforms" />
<img src="https://img.shields.io/badge/license-MIT-green?style=for-the-badge" alt="License" />

<br /><br />

# 👁️ react-native-spoiler-text

**A native, high-performance spoiler text component for React Native.**  
Blur your text. Reveal on tap. Built with Fabric / New Architecture support.

<br />

```
✦  Hide spoilers  ✦  Animate reveals  ✦  Fully native  ✦  Zero JS overhead  ✦
```

<br />

</div>

---

![rn spoiler view](https://github.com/user-attachments/assets/6d2413cc-7d89-4c8f-a03c-089ada26279e)

---

> [!NOTE]
> currently supported for Android only

## ✨ Features

- 🔥 **Fully native** — rendered in native UIKit (iOS) and Canvas/Paint (Android), no JS blur hacks
- ⚡ **New Architecture ready** — built with `codegenNativeComponent` and Fabric support
- 🎨 **Highly customizable** — font size, weight, family, style, line height, color
- 💨 **Smooth animations** — native blur radius animation on reveal
- 🖱️ **Tap to reveal** — built-in `onPress` handler via `DirectEventHandler`
- 🌓 **Dark mode friendly** — accepts `ColorValue` for full theme support

---

## 📦 Installation

```bash
# npm
npm install react-native-spoiler-text

# yarn
yarn add react-native-spoiler-text

# pnpm
pnpm add react-native-spoiler-text
```

### iOS — Pod install

```bash
cd ios && pod install
```

> **Requirements:**  
> React Native ≥ 0.73 · New Architecture enabled · iOS 13+ · Android API 21+

---

## 🚀 Quick Start

The simplest usage — a static spoiler toggled by state:

```tsx
import { useState } from 'react';
import { SpoilerTextView } from 'react-native-spoiler-text';

export default function App() {
  const [spoiler, setSpoiler] = useState(true);

  return (
    <SpoilerTextView
      text="Darth Vader is Luke's father."
      spoiler={spoiler}
      fontSize={16}
      fontWeight="600"
      color="#ffffff"
      onPress={() => setSpoiler(false)}
      style={{ padding: 12 }}
    />
  );
}
```

---

## 🎬 Animations with Reanimated

`SpoilerTextView` supports `react-native-reanimated` via `Animated.createAnimatedComponent`. The recommended pattern is:

- **`spoiler`** — controlled by React state (toggles the native blur layer on/off)
- **`blurRadius`** — driven by a Reanimated `useSharedValue` for smooth animated transitions

```tsx
import { useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { SpoilerTextView } from 'react-native-spoiler-text';
import Animated, {
  useAnimatedProps,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';

const AnimatedSpoilerTextView = Animated.createAnimatedComponent(SpoilerTextView);

export default function App() {
  const blur = useSharedValue(70);

  const animatedProps = useAnimatedProps(() => ({
    blurRadius: blur.value,
  }));

  const handlePress = () => {
    blur.value = withTiming(0, { duration: 300 });
  };

  return (
    <AnimatedSpoilerTextView
      animatedProps={animatedProps}
      text="The butler did it."
      fontSize={18}
      onPress={handlePress}
    />
  );
}
```

> ⚠️ **`spoiler` vs `blurRadius`** — these are independent:
> - `spoiler={true}` activates the particles layer. Set it via state.
> - `blurRadius` controls the blur intensity and is what you animate.
> - To fully reveal, animate `blurRadius` to `0` if using blur **and** set `spoiler={false}` if using spoiler.
> - we recommend to use it seperately

---

## 📝 Inline text with word-level spoilers

`SpoilerTextView` is an inline native view. When mixed with React Native `<Text>`, wrapping a long string in a single component would cause it to take up a full block even mid-sentence.

**The solution:** split your spoiler text by word, and render each word as its own `SpoilerTextView` inside a `<Text>` node. This preserves natural text flow and line wrapping:

```tsx
import { useState } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { SpoilerTextView } from 'react-native-spoiler-text';
import Animated, {
  useAnimatedProps,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';

const AnimatedSpoilerTextView = Animated.createAnimatedComponent(SpoilerTextView);

export default function App() {
  const spoilerText = 'this is an example of blur view';
  const [spoiler, setSpoiler] = useState(true);

  // Animated blur for the word-split spoiler
  const blur = useSharedValue(70);
  const animatedProps = useAnimatedProps(() => ({
    blurRadius: blur.value,
  }));

  const handlePress = () => {
    blur.value = withTiming(0, { duration: 300 });
  };

  return (
    <View style={styles.container}>
      <Text style={{ fontSize: 34 }}>
        This is normal text{' '}

        {/* Word-split spoiler — preserves inline text flow across line breaks */}
        {spoilerText.split(' ').map((word, index) => (
          <Text key={index}>
            <AnimatedSpoilerTextView
              animatedProps={animatedProps}
              fontSize={34}
              style={styles.box}
              text={word + ' '}
              onPress={handlePress}
            />
          </Text>
        ))}

        and this is normal text{' '}

        {/* Simple state-based spoiler */}
        <SpoilerTextView
          spoiler={spoiler}
          style={styles.box}
          text="hello 2"
          fontSize={34}
          onPress={() => setSpoiler(false)}
        />
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 30,
  },
  box: {
    overflow: 'visible',
  },
});
```

### Why split by word?

Without splitting, `SpoilerTextView` behaves as a block-level native view. If the text wraps to a new line, it reserves the full width of its container — leaving an awkward blank area. Splitting into per-word components lets each word flow naturally inline alongside regular `<Text>` content.

```
❌ Without splitting        ✅ With word splitting

This is normal [           This is normal this is
                           an example of blur view
                           ] and this is...
 spoiler block ]
and this is...
```

---

## 📐 Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `text` | `string` | — | The text content to display |
| `color` | `ColorValue` | `#000000` | Text color (supports theme tokens) |
| `spoiler` | `boolean` | `false` | Whether the spoiler blur is active |
| `blurRadius` | `Float` | `0` | Radius of the blur effect (0 = no blur) |
| `fontSize` | `Float` | `14` | Font size in pts/sp |
| `fontFamily` | `string` | System | Font family name |
| `fontWeight` | `string` | `"normal"` | `"normal"` · `"bold"` · `"100"`–`"900"` |
| `fontStyle` | `string` | `"normal"` | `"normal"` · `"italic"` |
| `lineHeight` | `Float` | — | Line height in pts/sp |
| `onPress` | `DirectEventHandler<{}>` | — | Callback fired when the component is tapped |
| `...ViewProps` | — | — | All standard `View` props are supported |

---

## 🧠 How It Works

`SpoilerTextView` is a **Fabric native component** registered via `codegenNativeComponent`. The blur effect is applied directly in:

- **iOS** — `UIVisualEffectView` / CoreImage blur applied to a `CALayer`
- **Android** — `RenderScript` / `BlurMaskFilter` on a native `Canvas`

This means **zero JavaScript bridge overhead** for the blur animation — the effect runs entirely on the UI thread at 60/120 fps.

```
JS Thread          UI Thread (Native)
    │                     │
    │── spoiler=true ─────▶  Apply blur via native layer
    │── blurRadius=10 ────▶  Animate blur natively
    │◀─ onPress ──────────── Dispatch tap event
```

---

## 🔧 Enabling New Architecture

Make sure New Architecture is enabled in your project.

**iOS** (`Podfile`):
```ruby
ENV['RCT_NEW_ARCH_ENABLED'] = '1'
```

**Android** (`gradle.properties`):
```properties
newArchEnabled=true
```

---

## 🤝 Contributing

Contributions, bug reports, and feature requests are welcome!

```bash
# Clone and bootstrap
git clone https://github.com/your-org/react-native-spoiler-text.git
cd react-native-spoiler-text
yarn install

# Run the example app
cd example && yarn install
yarn ios   # or yarn android
```

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) before submitting a pull request.

---

## 📄 License

MIT © [Divyanshu Patil](https://github.com/divyanshu-patil)

---

## Credits

- **Divyanshu Patil** – Author & maintainer
- Built with help from the open-source community ❤️

### special thanks to [Software-Mansion](https://github.com/software-mansion) for the custom shadow node code

checkout [react-native-enriched](https://github.com/software-mansion/react-native-enriched) by [software-mansion](https://github.com/software-mansion)

---

<div align="center">

Made with ❤️ for the React Native community

</div>
