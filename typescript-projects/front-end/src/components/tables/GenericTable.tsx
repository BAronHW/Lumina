import { Table } from '@mantine/core';

interface ColumnDef<T> {
  header: string;
  accessor: (row: T) => React.ReactNode;
}

interface TableProps<T> {
  columns: ColumnDef<T>[];
  rows: T[];
}

/**
 * This component is a generic table that can be used for all purposes of this application
 * The component accepts props of an array of TableRow which is represents the headers
 * of the table. On top of this there is the TableRow prop which is an array of objects
 * which represents 
 * @returns 
 */
export function GenericTable<T>({ columns, rows }: TableProps<T>) {

  return (
    <Table>
      <Table.Thead>
        <Table.Tr>
          {columns.map((col) => (
            <Table.Th key={col.header}>{ col.header }</Table.Th>
            ))}
        </Table.Tr>
      </Table.Thead>
      <Table.Tbody>
        {rows.map((row, index) => (
          <Table.Tr key={index}>
            {columns.map((col) => (
              <Table.Td key={col.header}>{col.accessor(row)}</Table.Td>
              ))}
          </Table.Tr>
          ))}
      </Table.Tbody>
    </Table>
  );
}