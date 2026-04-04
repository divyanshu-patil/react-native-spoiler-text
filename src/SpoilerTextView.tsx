import type { SpoilerTextViewProps } from './SpoilerTextViewNativeComponent';

/**
 * fallback for web and non native platforms
 */
export function SpoilerTextView(_props: SpoilerTextViewProps): never {
  throw new Error(
    "'react-native-blurtext' is only supported on native platforms."
  );
}
