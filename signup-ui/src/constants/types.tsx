type HeightWidth = {
  height: string;
  width: string;
};

type SizeValue = "small" | "medium" | "large";

type Sizes = { [key in SizeValue]: HeightWidth };

type StringMap = { [key: string]: string}
type StringNumberMap = { [key: string]: number}

type LabelValue = {
    label: string;
    value: string;
}

export type { Sizes, SizeValue, HeightWidth, StringMap, StringNumberMap, LabelValue };
