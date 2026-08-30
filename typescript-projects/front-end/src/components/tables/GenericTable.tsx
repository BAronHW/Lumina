import { Table } from '@mantine/core';

export interface ColumnDef<T> {
  header: string;
  accessor: (row: T) => React.ReactNode;
}

interface TableProps<T> {
  columns: ColumnDef<T>[];
  rows: T[];
  getRowKey: (row: T) => string;
  onRowClick: (row: T) => void
}

export function GenericTable<T>({ columns, rows, getRowKey, onRowClick }: TableProps<T>) {

  return (
    <Table highlightOnHover>
      <Table.Thead>
        <Table.Tr>
          {columns.map((col) => (
            <Table.Th key={col.header}>{ col.header }</Table.Th>
            ))}
        </Table.Tr>
      </Table.Thead>
      <Table.Tbody>
        {rows.map((row) => (
          <Table.Tr
            key={getRowKey(row)}
            onClick={() => onRowClick(row)}
            style={{ cursor: 'pointer' }}
          >
            {columns.map((col) => (
              <Table.Td key={col.header}>{col.accessor(row)}</Table.Td>
              ))}
          </Table.Tr>
          ))}
      </Table.Tbody>
    </Table>
  );
}