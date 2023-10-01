import argparse
import dataclasses
import datetime
import glob
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from xml.etree.ElementTree import Element
from typing import List
from dateutil import parser


@dataclasses.dataclass
class Record:
    date: datetime.datetime
    millis: int
    nanos: int
    sequence: int
    logger: str
    level: str
    class_name: str
    method: str
    thread: int
    message: str

    def __str__(self):
        methodname = self.logger+ "." + self.method
        return (f"{str(self.date)[:23]}|{methodname[:35]:<35}|"
                f"{self.thread:<3}|\t{self.level}:{self.message}")

    @staticmethod
    def from_element(element: Element) -> "Record":
        return Record(
            parser.parse(element.find("date").text),
            int(element.find("millis").text),
            int(element.find("nanos").text),
            int(element.find("sequence").text),
            element.find("logger").text,
            element.find("level").text,
            element.find("class").text,
            element.find("method").text,
            int(element.find("thread").text),
            element.find("message").text
        )

    @staticmethod
    def from_file(file_path: Path) -> List["Record"]:
        tree = ET.parse(file_path)
        elements = tree.findall("record")
        records = []
        for element in elements:
            records.append(Record.from_element(element))
        return records

    @staticmethod
    def from_dir(dir_path: Path) -> List["Record"]:
        file_list = glob.glob("*.log.*", root_dir=dir_path)
        records = []
        for file in file_list:
            records.extend(Record.from_file(dir_path / file))
        return sorted(records, key=lambda item: item.millis)

    @staticmethod
    def to_string(records: List["Record"]) -> str:
        return "\n".join([str(item) for item in records])


def main(argv=None):
    if argv is None:
        argv = sys.argv[1:]
    parser = argparse.ArgumentParser("XML Log Consume")
    parser.add_argument("--source_dir", type=str, help="Directory to xml log files")
    parser.add_argument(
        "--dest_dir", type=str, default=None,
        help="Directory to where new log file will be stored"
    )
    args = parser.parse_args(argv)
    source_path = Path(args.source_dir)
    records = Record.from_dir(source_path)
    current_time = datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S")
    dest_path = Path(args.dest_dir)
    dest_path.mkdir(parents=True, exist_ok=True)
    with open(dest_path / f"Aggregated_{current_time}.log", "w") as file:
        file.write(Record.to_string(records))


if __name__ == "__main__":
    sys.exit(main())
