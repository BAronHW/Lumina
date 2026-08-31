import { useMemo } from "react";

export interface FlameChartProps<T> {
    data: T[];
    getKey: (elem: T) => string;
    getParentKey: (elem: T) => string | null;
    getStart: (elem: T) => number;
    getDuration: (elem: T) => number;
    getLabel: (elem: T) => string;
    getColor: (elem: T) => string;
    getTooltip?: (elem: T) => string;
    onClick: (elem: T) => void;
    minBarWidth?: number;
    rowHeight?: number;
}

export default function FlameChart<T>(props: FlameChartProps<T>) {
    const { 
        data, 
        getKey, 
        getParentKey, 
        getStart, 
        getDuration, 
        getLabel, 
        getColor, 
        getTooltip, 
        onClick, 
        minBarWidth, 
        rowHeight 
    } = props;

    const childrenMap = data.reduce((map, item) => {
        const parentKey = getParentKey(item);
        const siblings = map.get(parentKey) ?? [];
        siblings.push(item);
        map.set(parentKey, siblings);
        return map;
    }, new Map<string | null, T[]>())

    const rootNode = data.find((elem) => !getParentKey(elem))

    
    /**
     * 1. x axis is duration and then y axis is the order
     * 2. write a function that would sort the data into order 
     * 3. some function that will determine where on the x and y axis the element will go depending on order and also duration of the element
     */

  return (
    <div>
        map
    </div>
  )
}
