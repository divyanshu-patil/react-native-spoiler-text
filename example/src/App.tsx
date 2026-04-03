import { useState } from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { BlurTextView } from 'react-native-blur-text';

export default function App() {
  const text = ` helcdvc hiee  g vfdv g fsfdvfd`;
  const [spoiler, setSpoiler] = useState(true);
  const [spoiler2, setSpoiler2] = useState(true);
  return (
    <View style={styles.container}>
      <Text>
        This is exa text view jj
        <BlurTextView
          spoiler={spoiler}
          style={styles.box}
          text={text}
          blurRadius={0}
          onPress={() => setSpoiler(false)}
        />{' '}
        hello cfdhvkj hdjvhj khjjfkdhkjvh{' '}
        <BlurTextView
          spoiler={spoiler2}
          style={styles.box}
          text={'hello 2'}
          blurRadius={7}
          color={'red'}
          onPress={() => setSpoiler2(false)}
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
    // width: 100,
    // height: 20,
    // paddingLeft: 10,
    // color: 'black',
    // paddingTop: 2,
    // backgroundColor: 'tomato',
  },
});
