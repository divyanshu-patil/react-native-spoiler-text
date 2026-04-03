import type { BlurTextViewProps } from './BlurTextViewNativeComponent';

/**
 * fallback for web and non native platforms
 */
export function BlurTextView(_props: BlurTextViewProps): never {
  throw new Error(
    "'react-native-blurtext' is only supported on native platforms."
  );
}
