import { useState } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { BlurTextView } from 'react-native-blur-text';
import Animated, {
  useAnimatedProps,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';

const AnimatedBlurTextView = Animated.createAnimatedComponent(BlurTextView);

export default function App() {
  const text = ` helcdg`;
  const [spoiler, setSpoiler] = useState(true);

  const blur = useSharedValue(40);

  const animatedProps = useAnimatedProps(() => ({
    blurRadius: blur.value,
  }));

  const handlePress = () => {
    console.log('PREASed');
    blur.value = withTiming(0, { duration: 300 });
  };

  return (
    <View style={styles.container}>
      <Text style={{ fontSize: 34 }}>
        This is exa text view
        <AnimatedBlurTextView
          animatedProps={animatedProps}
          fontSize={34}
          spoiler={false}
          style={styles.box}
          text={text}
          // blurRadius={15}
          onPress={() => {
            console.log('oreess');
            handlePress();
          }}
        />{' '}
        hello cfdhvkj hdjvhj khjjfkdhkjvh{' '}
        <BlurTextView
          spoiler={spoiler}
          style={styles.box}
          text={'hello 2'}
          // color={'red'}
          onPress={() => {
            setSpoiler(false);
          }}
          // blurRadius={20}
          fontSize={34}
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
    // backgroundColor: '#e0a333',
    overflow: 'visible',
    // transform: [{ translateY: 2 }],
  },
});
