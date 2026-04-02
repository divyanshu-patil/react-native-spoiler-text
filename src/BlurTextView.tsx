import type { ColorValue, ViewProps } from 'react-native';

type Props = ViewProps & {
  color?: ColorValue;
};

export function BlurTextView(_props: Props): never {
  throw new Error(
    "'react-native-blur-text' is only supported on native platforms."
  );
}
