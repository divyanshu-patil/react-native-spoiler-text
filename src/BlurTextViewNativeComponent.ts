import { codegenNativeComponent } from 'react-native';
import type { HostComponent } from 'react-native';
import type { ColorValue, ViewProps } from 'react-native';
import type {
  DirectEventHandler,
  Float,
} from 'react-native/Libraries/Types/CodegenTypes';

export default codegenNativeComponent<BlurTextViewProps>('BlurTextView', {
  interfaceOnly: true,
}) as HostComponent<BlurTextViewProps>;

export interface BlurTextViewProps extends ViewProps {
  /**
   * Text color.
   */
  text?: string;

  /**
   * Text color.
   */
  color?: ColorValue;

  /**
   * Blur radius
   */
  blurRadius?: Float;

  /**
   * Blur radius
   */
  spoiler?: boolean;

  /**
   * Font size of the text.
   */
  fontSize?: Float;

  /**
   * Font family name.
   */
  fontFamily?: string;

  /**
   * Font weight.
   *
   * Example values: `"normal"`, `"bold"`, `"100"`–`"900"`.
   */
  fontWeight?: string;

  /**
   * Font style.
   *
   * Example values: `"normal"`, `"italic"`.
   */
  fontStyle?: string;

  /**
   * Line height of the text.
   */
  lineHeight?: Float;

  onPress?: DirectEventHandler<{}>;
}
