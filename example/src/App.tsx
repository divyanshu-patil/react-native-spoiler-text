import { View, StyleSheet, Text } from 'react-native';
import { BlurTextView } from 'react-native-blur-text';

export default function App() {
  const text = ` helcdvc hiee  ghjfdvfd`;
  return (
    <View style={styles.container}>
      <Text>
        This is exa text view
        <BlurTextView style={styles.box} text={text} blurRadius={0} /> hello
        cfdhvkj hdjvhj khjjfkdhkjvh{' '}
        <BlurTextView
          style={styles.box}
          text={'hello 2'}
          blurRadius={7}
          color={'red'}
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
