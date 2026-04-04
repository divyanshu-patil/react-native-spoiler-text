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
  const text = `this  is an example of blur view`;
  const [spoiler, setSpoiler] = useState(true);

  const blur = useSharedValue(70);

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
        This is normal text{' '}
        {text.split(' ').map((item, index) => (
          <Text key={index}>
            <AnimatedBlurTextView
              animatedProps={animatedProps}
              fontSize={34}
              // spoiler={spoiler}
              style={styles.box}
              text={item + ' '}
              // blurRadius={15}
              onPress={() => {
                console.log('oreess');
                handlePress();
                // setSpoiler(false);
              }}
            />
          </Text>
        ))}
        and this is normal text{' '}
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
